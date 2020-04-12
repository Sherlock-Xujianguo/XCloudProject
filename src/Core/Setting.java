package Core;

public class Setting {

    public static final String _homePath = System.getProperty("user.home");
    public static final String _envSep = System.getProperty("file.separator");

    public static class Client {
        public static final String _defaultDirectoryPath = _homePath + _envSep + "Documents" + _envSep + "XCloud";
        public static final String _fileTreeDataPath = _defaultDirectoryPath + _envSep + ".temp" + _envSep + "FileTree";
        public static final String _fileTreeDataName = Client._fileTreeDataPath + Setting._envSep + "FileTree.dat";
    }

    public static class Server {
        public static final String _defaultDirectoryPath = _homePath + _envSep + "XCloud";
        public static final String _fileTreeDataPath = _defaultDirectoryPath + _envSep + ".temp" + _envSep + "FileTree";
        public static final String _fileTreeDataName = Server._fileTreeDataPath + Setting._envSep + "FileTree.dat";
    }


    public static void main(String[] args) {
        System.out.println(_envSep);
    }
}
