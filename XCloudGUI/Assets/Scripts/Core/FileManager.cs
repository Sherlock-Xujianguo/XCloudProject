using System.Collections;
using System.Collections.Generic;
using System.IO;
using UnityEngine;

public class FileManager
{
    public static FileManager Instance = new FileManager();
    private FileManager() { }

    string _bindDirectory = Setting.defaultDirectory;

    public int UpdateBindDirectory(string path)
    {
        if (File.Exists(path))
        {
            return 1;
        }
        else if (!Directory.Exists(path))
        {
            return 2;
        }
        _bindDirectory = path;
        return 0;
    }


}
