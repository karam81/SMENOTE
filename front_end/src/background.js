"use strict";

import { app, protocol, BrowserWindow, Menu, dialog } from "electron";
import { createProtocol } from "vue-cli-plugin-electron-builder/lib";
import installExtension, { VUEJS_DEVTOOLS } from "electron-devtools-installer";
import menuTemplate from './markdown/menuTemplate.js';
import fs from "fs";

const isDevelopment = process.env.NODE_ENV !== "production";

// Keep a global reference of the window object, if you don't, the window will
// be closed automatically when the JavaScript object is garbage collected.

// let win = Electron.BrowserWindow;
let win;

// let win: Electron.BrowserWindow | null;


// Scheme must be registered before the app is ready
protocol.registerSchemesAsPrivileged([
  { scheme: "app", privileges: { secure: true, standard: true } }
]);

function createWindow() {
  // Create the browser window.
  win = new BrowserWindow({
    width: 1000,
    height: 600,
    webPreferences: {
      // Use pluginOptions.nodeIntegration, leave this alone
      // See nklayman.github.io/vue-cli-plugin-electron-builder/guide/security.html#node-integration for more info
      nodeIntegration: (process.env.ELECTRON_NODE_INTEGRATION)
      //true
      
        // .ELECTRON_NODE_INTEGRATION as unknown) as boolean
    }
  });

  if (process.env.WEBPACK_DEV_SERVER_URL) {
    // Load the url of the dev server if in development mode
    win.loadURL(process.env.WEBPACK_DEV_SERVER_URL);
    // win.loadURL(process.env.WEBPACK_DEV_SERVER_URL as string);
    if (!process.env.IS_TEST) win.webContents.openDevTools();
  } else {
    createProtocol("app");
    // Load the index.html when not in development
    win.loadURL("app://./index.html");
  }

  win.on("close", (event) => {
    console.log("win.on(close) 호출됨.");

    event.preventDefault();
    
    const options = {
      type: "question",
      title: "Question",
      message: "Are you sure you want to quit without saving?",
      detail: "Click the save button if you want to save this text to your md file",
      buttons: ["Cancel", "Save"],
      defaultId: 1
    };

    dialog.showMessageBox(options)
    .then(result => {

      // 1 : Save
      if(result.response == 1) {
        var fileData = '';

        BrowserWindow.getFocusedWindow().webContents.executeJavaScript(`document.getElementById("editor_textarea").value`)
        .then(result => {
            fileData = result;
        });

        dialog.showSaveDialog(
            {
                title: "파일 저장하기!!!",
                filters: [
                    { name: 'Markdown', extensions: ['md'] },
                ],
                message: "TEST"
            }
        )
        .then(result => {
          console.log(result.filePath);

            var fileName = result.filePath;
            fs.writeFile(fileName, fileData, (err) => {

            })
            
            BrowserWindow.getFocusedWindow().destroy();
        });
      }

      // 2 : Exit window without save
      else if(result.response == 0) {
        BrowserWindow.getFocusedWindow().destroy();
      }
    });
  });

  win.on("closed", () => {
    win = null;
  });

  win.webContents.closeDevTools();
  win.setTitle("소망이노트");
  win.on('page-title-updated', function(e) {
    e.preventDefault()
  });
}

const menu = Menu.buildFromTemplate(menuTemplate);
Menu.setApplicationMenu(menu);

// Quit when all windows are closed.
app.on("window-all-closed", () => {
  // On macOS it is common for applications and their menu bar
  // to stay active until the user quits explicitly with Cmd + Q
  if (process.platform !== "darwin") {
    app.quit();
  }
});

app.on("activate", () => {
  // On macOS it's common to re-create a window in the app when the
  // dock icon is clicked and there are no other windows open.
  if (win === null) {
    createWindow();
  }
});

// This method will be called when Electron has finished
// initialization and is ready to create browser windows.
// Some APIs can only be used after this event occurs.
app.on("ready", async () => {
  if (isDevelopment && !process.env.IS_TEST) {
    // Install Vue Devtools
    try {
      await installExtension(VUEJS_DEVTOOLS);
    } catch (e) {
      console.error("Vue Devtools failed to install:", e.toString());
    }
  }
  createWindow();
});

// Exit cleanly on request from parent process in development mode.
if (isDevelopment) {
  if (process.platform === "win32") {
    process.on("message", data => {
      if (data === "graceful-exit") {
        app.quit();
      }
    });
  } else {
    process.on("SIGTERM", () => {
      app.quit();
    });
  }
}

export {createWindow};