import { app, BrowserWindow, dialog } from "electron";
import { createWindow } from "../background";
const fs = require("fs");
const isMac = process.platform === "darwin"

const {ipcMain} = require('electron');

export let openedFileData = "test";

const template = [
    // { role: 'appMenu' }
    ...(isMac ? [{
        label: app.name,
        submenu: [
        { role: 'about' },
        { type: 'separator' },
        { role: 'services' },
        { type: 'separator' },
        { role: 'hide' },
        { role: 'hideothers' },
        { role: 'unhide' },
        { type: 'separator' },
        { role: 'quit' }
        ]
    }] : []),
    // { role: 'fileMenu' }
    {
        label: 'File',
        submenu: [
            isMac ? { role: 'close' } : { role: 'quit' },
            {
                label: 'New Window',
                accelerator: 'CommandOrControl+N',
                click() {
                    openNewWindow();
                }
            },
            {
                label: 'Save As ...',
                accelerator: 'CommandOrControl+S',
                click() {
                    saveFile();
                }
            },
            {
                label: 'Open',
                accelerator: 'CommandOrControl+O',
                click() {
                    openFile();
                }
            },
        ]
    },
    // { role: 'editMenu' }
    {
        label: 'Edit',
        submenu: [
            { role: 'undo' },
            { role: 'redo' },
            { type: 'separator' },
            { role: 'cut' },
            { role: 'copy' },
            { role: 'paste' },
            ...(isMac ? [
                { role: 'pasteAndMatchStyle' },
                { role: 'delete' },
                { role: 'selectAll' },
                { type: 'separator' },
                {
                label: 'Speech',
                submenu: [
                    { role: 'startspeaking' },
                    { role: 'stopspeaking' }
                ]
                }
            ] : [
                { role: 'delete' },
                { type: 'separator' },
                { role: 'selectAll' }
            ])
        ]
    },
    // { role: 'viewMenu' }
    {
        label: 'View',
        submenu: [
            { role: 'reload' },
            { role: 'forcereload' },
            { role: 'toggledevtools' },
            { type: 'separator' },
            { role: 'resetzoom' },
            { role: 'zoomin' },
            { role: 'zoomout' },
            { type: 'separator' },
            { role: 'togglefullscreen' },
            { type: 'separator' },
            { role: 'toggleDevTools'}
        ]
    },
    // { role: 'windowMenu' }
    {
        label: 'Window',
        submenu: [
            { role: 'minimize' },
            { role: 'zoom' },
            ...(isMac ? [
                { type: 'separator' },
                { role: 'front' },
                { type: 'separator' },
                { role: 'window' }
            ] : [
                { role: 'close' }
            ])
        ]
    },
    {
      role: 'help',
      submenu: [
        {
            label: 'Markdown Help',
        accelerator: 'F1',
        click() {
            createHelpWindow();
            }
        },
        {
          label: 'Learn More',
          click () { require('electron').shell.openExternal('https://electronjs.org') }
        }
      ]
    }
];
  
function openNewWindow() {
    createWindow();
}

function saveFile() {
    var fileData = '';

    BrowserWindow.getFocusedWindow().webContents.executeJavaScript(`document.getElementById("editor_textarea").value`)
    .then(result => {
        fileData = result;
        // console.log(fileData);
    });

    var fileName = dialog.showSaveDialog(BrowserWindow.getFocusedWindow(),
        {
            title: "파일 저장하기!!!",
            filters: [
                { name: 'Markdown', extensions: ['md'] },
            ],
            message: "TEST"
        }
    )
    .then(result => {
        fileName = result.filePath;
        fs.writeFile(fileName, fileData, (err) => {

        })
    });
}

function openFile() {
    var fileData = '';
    
    BrowserWindow.getFocusedWindow().webContents.executeJavaScript(`document.getElementById("editor_textarea").value`)
    .then(result => {
        fileData = result;

        // console.log(typeof fileData);
        // console.log(fileData);

        // 작성중인 텍스트가 있다면, 저장할건지 먼저 물어본 뒤 파일을 열기.
        if(fileData.length > 0) {
            const options = {
                type: "question",
                title: "Question",
                message: "Are you sure you want to quit without saving?",
                detail: "Click the save button if you want to save this text to your md file",
                buttons: ["Cancel", "Save"],
                defaultId: 1
            };
      
            if(dialog.showMessageBoxSync(options) == 1) {
                // 1 : Save
                if (result.response == 1) {
                    var fileData = '';
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
                        // console.log(result.filePath);
            
                        var fileName = result.filePath;
                        fs.writeFile(fileName, fileData, (err) => {
        
                        })
                    });
                }
            }
        }
        
        dialog.showOpenDialog(BrowserWindow.getFocusedWindow(),
            {
                filters: [
                    { name: 'Markdown', extensions: ['md'] }
                ]
            }
        )
        .then(result => {
            let absoluteFilePath = JSON.stringify(result.filePaths[0]);
            absoluteFilePath = absoluteFilePath.substr(1, absoluteFilePath.length - 2)

            fs.readFile(absoluteFilePath, 'utf8', (err, data) => {
                // console.log(absoluteFilePath);
                if (err) throw err;
                // console.log(data);
                // fileData = data;
                openedFileData = data;
                // console.log(openedFileData);

                let fileDataObject = {'openedFileData': openedFileData, 'absoluteFilePath': absoluteFilePath};
                let win = BrowserWindow.getFocusedWindow();
                win.webContents.send("ping", fileDataObject);
            });
        });
    });
}

function createHelpWindow() {
    console.log(`file://${__dirname}/app/help.html`)
    // Create the browser window.
    let win = new BrowserWindow({
      width: 600,
      height: 400,
      webPreferences: {
        nodeIntegration: true
      }
    });
    win.loadURL(`file://${__dirname}/../src/help.html`)
    win.setTitle("도움말");
    win.on('page-title-updated', function(e) {
    e.preventDefault()
  });
  }

export default template
