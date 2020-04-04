
public class HashCipher {

	static int args[] = null; 
		
	public static String Cihper(String originalText) {
		args = new int[26];
		
		String cihperText = "";
		
		for(int i =0;i<26;i++) {
			args[i] = 0;
		}
		
		for(int i = 0;i<originalText.length();i++) {
			char c = originalText.charAt(i);
			
			if(c>='a'&&c<='z') {
				args[c-'a']++;
			}
			
			else if(c>='A'&&c<='Z') {
				args[c-'A']++;
			}
			
			cihperText = cihperText+(char)(c+10);
			
		}
		
		for(int i =0;i<5;i++) {
			char c = 'a';
			for(int j = 0;j<26;j++) {
				if(args[j] == 0) {
					c+=j;
					args[j] = 1;
					//System.out.println(c);
					break;
				}
			}
			cihperText = cihperText+c;
		}
		return cihperText;
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		System.out.println(HashCipher.Cihper("PassWord"));
	}

}
