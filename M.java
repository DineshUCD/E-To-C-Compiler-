public class M {

  public static void main(String[] args) {
    try {
      throw new MyException2("Wrong input");

    } catch(Exception e) {
      System.out.println("Exception caught.");
    }
  }

}
