package com.azsoft.ecams.core;

import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleFactory;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.MessageConsole;

public class Factory implements IConsoleFactory {

    private static MessageConsole _console;

    public void openConsole() {
        MessageConsole console = getConsole();
        if (console != null) {
            console.activate();
            
            IConsoleManager manager =
            ConsolePlugin.getDefault().getConsoleManager();
            IConsole[] existing = manager.getConsoles();
            boolean exists = false;
            for (int i = 0; i < existing.length; i++) {
                if(console == existing[i])
                    exists = true;
            }
            if(!exists){
                manager.addConsoles(new IConsole[] {console});
            }
            manager.showConsoleView(console);
        }
    }

    public static MessageConsole getConsole() {
        if (_console == null) {
            _console = new MessageConsole("eCAMS Console",null);
        }
        _console.activate();
        return _console;
    }

}
