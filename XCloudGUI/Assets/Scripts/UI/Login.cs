using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using UnityEngine.UI;

public class Login : MonoBehaviour
{
    public GameObject _logInPanel;
    public InputField _userName;
    public InputField _passwd;
    public InputField _vertifyPasswd;
    public GameObject _alreadyHaveText;
    public GameObject _notCorrectText;
    public GameObject _notSameText;
    public GameObject _signInButtonList;
    public GameObject _signUpButtonList;


    void Start()
    {
        
    }

    // Update is called once per frame
    void Update()
    {
        
    }

    public void SignIn()
    {
        string userName = _userName.text;
        Debug.Log(userName);
        string passwd = _passwd.text;
        Debug.Log(passwd);
        UserManager.Instance.Init(userName, passwd);
        int result = ServerSocket.Instance.LogIn();
        if (result == 0)
        {
            Debug.Log("登录成功");
            _notCorrectText.SetActive(false);
            _logInPanel.SetActive(false);
        }
        else
        {
            _notCorrectText.SetActive(true);
        }
    }

    public void ToSignUp()
    {
        _notCorrectText.SetActive(false);
        _signInButtonList.SetActive(false);
        _signUpButtonList.SetActive(false);

        _vertifyPasswd.gameObject.SetActive(true);
        _userName.text = "";
        _passwd.text = "";
        _vertifyPasswd.text = "";
        _signInButtonList.gameObject.SetActive(false);
        _signUpButtonList.gameObject.SetActive(true);
    }

    public void BackToSignIn()
    {
        _notCorrectText.SetActive(false);
        _signInButtonList.SetActive(false);
        _signUpButtonList.SetActive(false);

        _vertifyPasswd.gameObject.SetActive(false);
        _userName.text = "";
        _passwd.text = "";
        _vertifyPasswd.text = "";
        _signInButtonList.gameObject.SetActive(true);
        _signUpButtonList.gameObject.SetActive(false);
    }

    public void SignUp()
    {
        string userName = _userName.text;
        Debug.Log(userName);
        string passwd = _passwd.text;
        Debug.Log(passwd);
        string vertifyPasswd = _vertifyPasswd.text;
        if (passwd != vertifyPasswd)
        {
            _notSameText.SetActive(true);
            _passwd.text = "";
            _vertifyPasswd.text = "";
            return;
        }
        _notSameText.SetActive(false);
        UserManager.Instance.Init(userName, passwd);
        int result = ServerSocket.Instance.Sign();
        if (result == 0)
        {
            Debug.Log("注册成功");
            _alreadyHaveText.SetActive(false);
            BackToSignIn();
        }
        else
        {
            _alreadyHaveText.SetActive(true);
        }
    }
}
