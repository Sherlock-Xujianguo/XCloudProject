using System;
using System.Collections;
using System.Collections.Generic;
using Unity.DocZh.Components;
using UnityEngine;

public class MainMenu : MonoBehaviour
{
    public GameObject SettingPanel;
    public GameObject ReStart;

    public void OpenDir()
    {
        OpenDirectory.Open(FileManager._bindDirectory);
    }

    public void OpenSetting()
    {
        if (SettingPanel.activeSelf)
        {
            SettingPanel.SetActive(false);
        }
        else
        {
            SettingPanel.SetActive(true);
        }
    }

    public void Pause2()
    {
        Pause(2);
    }

    public void Pause8()
    {
        Pause(8);
    }

    public void Pause24()
    {
        Pause(24);
    }

    public void Pause(int hours) 
    {
        FileManager._startTime = DateTime.Now + new TimeSpan(hours, 0, 0);
        ReStart.SetActive(true);
        SettingPanel.SetActive(false);
    }

    public void Restart()
    {
        ReStart.SetActive(false);
        FileManager._startTime = DateTime.Now;
    }
}
