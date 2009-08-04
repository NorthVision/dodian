import java.io.*;

public class WeaponHandler
{

    public static int SA = 808;
    public static int WA = 819;
    public static int RA = 824;
    public static int AA = 422;
    public static int BA = 1834;
    public static int StandAn;
    public static int WalkAn;
    public static int RunAn;
    public static int AttackAn;
    public static int BlockAn;
    public static int AttackOption;

    public WeaponHandler()
    {
        System.out.println("WeaponHandler \251Mr. Brightsite");
    }

    public static void WeaponAnim(int Weapon)
    {
        String line = "";
        String token = "";
        String token2 = "";
        String token2_2 = "";
        String token3[] = new String[12];
        boolean EndOfFile = false;
        int ReadMode = 0;
        BufferedReader characterfile = null;
        try
        {
            characterfile = new BufferedReader(new FileReader("config\\weaponHandler.cfg"));
        }
        catch(FileNotFoundException fileex)
        {
            misc.println("weaponHandler.cfg: file not found.");
        }
        try
        {
            line = characterfile.readLine();
        }
        catch(IOException ioexception)
        {
            misc.println("weaponHandler.cfg: error loading file.");
        }
        while(!EndOfFile && line != null) 
        {
            line = line.trim();
            int spot = line.indexOf("=");
            if(spot > -1)
            {
                token = line.substring(0, spot);
                token = token.trim();
                token2 = line.substring(spot + 1);
                token2 = token2.trim();
                token2_2 = token2.replaceAll("\t\t", "\t");
                token2_2 = token2_2.replaceAll("\t\t", "\t");
                token2_2 = token2_2.replaceAll("\t\t", "\t");
                token2_2 = token2_2.replaceAll("\t\t", "\t");
                token2_2 = token2_2.replaceAll("\t\t", "\t");
                token3 = token2_2.split("\t");
                if(token.equals("id") && Weapon == Integer.parseInt(token3[0]))
                {
                    int FightType = Integer.parseInt(token3[4]);
                    if(Integer.parseInt(token3[1]) != -1)
                        StandAn = Integer.parseInt(token3[1]);
                    else
                        StandAn = SA;
                    if(Integer.parseInt(token3[2]) != -1)
                        WalkAn = Integer.parseInt(token3[2]);
                    else
                        WalkAn = WA;
                    if(Integer.parseInt(token3[3]) != -1)
                        RunAn = Integer.parseInt(token3[3]);
                    else
                        RunAn = RA;
                    if(Integer.parseInt(token3[5]) != -1)
                        AttackAn = Integer.parseInt(token3[5]);
                    else
                        AttackAn = AA;
                    if(Integer.parseInt(token3[9]) != -1)
                        BlockAn = Integer.parseInt(token3[9]);
                    else
                        BlockAn = BA;
                    if(FightType >= 0)
                    {
                        if(AttackOption == 1)
                            AttackAn = Integer.parseInt(token3[5]);
                        if(AttackOption == 2)
                            AttackAn = Integer.parseInt(token3[6]);
                        if(AttackOption == 3)
                            AttackAn = Integer.parseInt(token3[7]);
                        if(AttackOption == 4)
                            AttackAn = Integer.parseInt(token3[8]);
                    }
                }
            } else
            if(line.equals("[ENDOFWEAPONS]"))
                try
                {
                    characterfile.close();
                }
                catch(IOException ioexception) { }
            try
            {
                line = characterfile.readLine();
            }
            catch(IOException ioexception1)
            {
                EndOfFile = true;
            }
        }
        try
        {
            characterfile.close();
        }
        catch(IOException ioexception) { }
    }

}