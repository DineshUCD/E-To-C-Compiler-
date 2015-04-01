import java.lang.System.*;

class Generate {

  private static String cCode = new String("");

  public static void println(String translation)
  {
    cCode = translation;
    System.out.println(cCode);
  }

  public static void print(String translation)
  {
    cCode = translation;
    System.out.print(cCode);
  }
}

