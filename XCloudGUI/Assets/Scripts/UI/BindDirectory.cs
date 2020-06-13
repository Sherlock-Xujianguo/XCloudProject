using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using UnityEngine.UI;

public class BindDirectory : MonoBehaviour
{
    public GameObject _bindDirectoryPanel;
    public InputField _path;
    public Text _tips;

    void Start()
    {
        _path.text = Setting.defaultDirectory;
    }

    // Update is called once per frame
    void Update()
    {
        
    }

    public void Confirm()
    {
        _tips.gameObject.SetActive(false);

        int result = FileManager.Instance.UpdateBindDirectory(_path.text);

        if (result == 0)
        {
            _bindDirectoryPanel.SetActive(false);
            FileManager.Instance.Start();

            return;
        }
        else if (result == 1)
        {
            _tips.gameObject.SetActive(true);
            _tips.text = "目标不是文件夹";
        }
        else if (result == 2)
        {
            _tips.gameObject.SetActive(true);
            _tips.text = "目标路径不存在";
        }

    }
}
