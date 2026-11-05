public class Report {

    public void createReport(String level, String recommendation, int points) {

        System.out.println("\n================================");
        System.out.println("       ИТОГОВЫЙ ОТЧЁТ");
        System.out.println("================================");

        System.out.println("Уровень пользователя: " + level);
        System.out.println("Количество баллов: " + points + "/5");
        System.out.println("Рекомендация: " + recommendation);

        System.out.println("================================");
        System.out.println("      АНАЛИЗ ЗАВЕРШЁН");
        System.out.println("================================");
    }
}