/*
 *  Copyright (C) 2014  Alfons Wirtz  
 *   website www.freerouting.net
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License at <http://www.gnu.org/licenses/> 
 *   for more details.
 *
 * MainApplication.java
 *
 * Created on 19. Oktober 2002, 17:58
 *
 */
package gui;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Scanner;

import board.TestLevel;

/**
 *
 * Main application for creating frames with new or existing board designs.
 *
 * @author  Alfons Wirtz
 */
public class MainApplication extends javax.swing.JFrame{
	
	private final static String CONFIGS = "DATA";

	private static final long serialVersionUID = 1L;
	
	/**
     * Main function of the Application
     */
    public static void main(String p_args[])
    {
        String design_dir_name = loadLocation();
        
        java.util.Locale current_locale = java.util.Locale.ENGLISH;

        new MainApplication(design_dir_name, false, false, current_locale).setVisible(false);
    }

    /**
     * Creates new form MainApplication
     * It takes the directory of the board designs as optional argument.
     */
    public MainApplication(String p_design_dir, boolean p_is_test_version, boolean p_webstart_option, java.util.Locale p_current_locale)
    {
        this.design_dir_name = p_design_dir;
        this.locale = p_current_locale;
        this.resources = java.util.ResourceBundle.getBundle("gui.resources.MainApplication", p_current_locale);
        
        open_board_design_action(null);

    }

    /** opens a board design from a binary file or a specctra dsn file. */
    public void open_board_design_action(java.awt.event.ActionEvent evt)
    {

        DesignFile design_file = DesignFile.open_dialog(this.is_webstart, this.design_dir_name);

        if (design_file == null)
        {
            message_field.setText(resources.getString("message_3"));
            return;
        }

        BoardFrame.Option option;

        option = BoardFrame.Option.FROM_START_MENU;
        
        BoardFrame new_frame = null;
		
        try {
			new_frame = create_board_frame(design_file, message_field, option, this.is_test_version, this.locale);
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        if (new_frame == null)
        {
            return;
        }
        board_frames.add(new_frame);
        new_frame.addWindowListener(new BoardFrameWindowListener(new_frame));
    }

    /**
     * Creates a new board frame containing the data of the input design file.
     * Returns null, if an error occured.
     * @throws ClassNotFoundException 
     */
    public BoardFrame create_board_frame(DesignFile p_design_file, javax.swing.JTextField p_message_field,
            BoardFrame.Option p_option, boolean p_is_test_version, java.util.Locale p_locale) throws ClassNotFoundException
    {
        java.util.ResourceBundle resources = java.util.ResourceBundle.getBundle("gui.resources.MainApplication", p_locale);

        java.io.InputStream input_stream = p_design_file.get_input_stream();
        if (input_stream == null)
        {
            if (p_message_field != null)
            {
                p_message_field.setText(resources.getString("message_8") + " " + p_design_file.get_name());
            }
            return null;
        }
         
        BoardFrame new_frame = null;
        boolean read_ok = false;
        
		new_frame = new BoardFrame(p_design_file, p_option, TestLevel.RELEASE_VERSION, p_locale, !p_is_test_version, this);
		read_ok = new_frame.read(input_stream, p_design_file.is_created_from_text_file(), p_message_field);   		
        
		if (!read_ok)
        {
            return null;
        }
		
		saveLocation(p_design_file.get_Path());
		
        new_frame.menubar.add_design_dependent_items();
        if (p_design_file.is_created_from_text_file())
        {
            // Read the file  with the saved rules, if it is existing.

            String file_name = p_design_file.get_name();
            String[] name_parts = file_name.split("\\.");
            String confirm_import_rules_message = resources.getString("confirm_import_rules");
            DesignFile.read_rules_file(name_parts[0], p_design_file.get_parent(),
                    new_frame.board_panel.board_handling, p_option == BoardFrame.Option.WEBSTART,
                    confirm_import_rules_message);
            new_frame.refresh_windows();
        }
        return new_frame;
    }
    private final java.util.ResourceBundle resources;
    private javax.swing.JTextField message_field;
   
    /** The list of open board frames */
    private java.util.Collection<BoardFrame> board_frames = new java.util.LinkedList<BoardFrame>();
    private String design_dir_name = null;
    private final boolean is_test_version = false;
    private final boolean is_webstart = false;
    private final java.util.Locale locale;
   
    private class BoardFrameWindowListener extends java.awt.event.WindowAdapter
    {

        public BoardFrameWindowListener(BoardFrame p_board_frame)
        {
            this.board_frame = p_board_frame;
        }

        public void windowClosed(java.awt.event.WindowEvent evt)
        {
            if (board_frame != null)
            {
                // remove this board_frame from the list of board frames
                board_frame.dispose();
                board_frames.remove(board_frame);
                board_frame = null;
            }
        }
        private BoardFrame board_frame;
    }

    private void saveLocation(String location){
    	this.design_dir_name = location;
    	
    	PrintWriter pw = null;
    	try {
			pw = new PrintWriter(CONFIGS);
			pw.write(location);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
    	finally{
    		if(pw != null){
    			pw.close();
    		}
    	}	
    }
    
    private static String loadLocation(){
    	Scanner io = null;
    	
    	try {
			io = new Scanner(new File(CONFIGS));
			
			return io.nextLine();
			
		} catch (FileNotFoundException e) {
			//The file doesnt exist
		}finally{
			if(io != null){
				io.close();
			}
		}

    	return null;
    }

    /**
     * Change this string when creating a new version
     */
    static final String VERSION_NUMBER_STRING = "Lisbon";
}
