using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using System;
using System.Threading;
using System.Diagnostics;

public class OpenDirectory
{
    public static void Open(string path)
    {
        // 新开线程防止锁死
        Thread newThread = new Thread(new ParameterizedThreadStart(CmdOpenDirectory));
        newThread.Start(path);
    }

    private static void CmdOpenDirectory(object obj)
        {
            Process p = new Process();
            p.StartInfo.FileName = "cmd.exe";
            p.StartInfo.Arguments = "/c start " + obj.ToString();
            UnityEngine.Debug.Log(p.StartInfo.Arguments);
            p.StartInfo.UseShellExecute = false;
            p.StartInfo.RedirectStandardInput = true;
            p.StartInfo.RedirectStandardOutput = true;
            p.StartInfo.RedirectStandardError = true;
            p.StartInfo.CreateNoWindow = true;
            p.Start();

            p.WaitForExit();
            p.Close();
        }
}
