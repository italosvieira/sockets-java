import java.text.SimpleDateFormat;
import java.util.Date;

class Logger {
    static void info(String msg) {
        System.out.println(new SimpleDateFormat("HH:mm:ss dd/MM/yyyy").format(new Date()) + " INFO --- " + msg);
    }

    static void infoSameLine(String msg) {
        System.out.print(new SimpleDateFormat("HH:mm:ss dd/MM/yyyy").format(new Date()) + " INFO --- " + msg);
    }

    static void error(String msg, Exception e) {
        System.out.println(new SimpleDateFormat("HH:mm:ss dd/MM/yyyy").format(new Date()) + " ERROR --- " + msg + " Com a mensagem de erro: " + e.getMessage());
        e.printStackTrace();
    }
}