﻿using System.Collections;
using System.Collections.Generic;
using System.Text;
using UnityEngine;

public class SocketTest : MonoBehaviour
{
    // Start is called before the first frame update
    void Start()
    {
        
    }

    private void OnApplicationQuit()
    {
        FileManager.t.Abort();
    }

}
