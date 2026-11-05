import java.util.Scanner;

public class DecisionSystem {

    private final Scanner scanner;

    public DecisionSystem(Scanner scanner) {
        this.scanner = scanner;
    }

    public void chooseEntertainment() {
        System.out.println("\n=== ПОДБОР РАЗВЛЕЧЕНИЯ ===");

        System.out.print("Введите ваше имя: ");
        String name = scanner.next();

        Validation validation = new Validation();

        if (!validation.checkName(name)) {
            return;
        }

        System.out.print("Сколько у вас свободных часов? ");
        int hours = scanner.nextInt();

        if (!validation.checkNumber(hours)) {
            return;
        }

        System.out.println("\nВыберите настроение:");
        System.out.println("1. Хорошее");
        System.out.println("2. Усталое");
        System.out.println("3. Скучное");

        System.out.print("Ваш выбор: ");
        int mood = scanner.nextInt();

        if (mood == 1) {
            System.out.println(name + ", рекомендуем прогулку!");
        } else if (mood == 2) {
            System.out.println(name + ", рекомендуем посмотреть фильм!");
        } else if (mood == 3) {
            System.out.println(name + ", рекомендуем поиграть!");
        } else {
            System.out.println("Ошибка! Такого настроения нет.");
        }
    }

    public void checkStatus(int status) {
        System.out.println("\n=== СТАТУС ПОЛЬЗОВАТЕЛЯ ===");

        if (status == 1) {
            System.out.println("Статус: Активный");
        } else if (status == 2) {
            System.out.println("Статус: Неактивный");
        } else if (status == 3) {
            System.out.println("Статус: Заблокированный");
        } else {
            System.out.println("Неизвестный статус!");
        }
    }
    public void checkAge(int age) {
        System.out.println("\n=== ВОЗРАСТНАЯ КАТЕГОРИЯ ===");

        if (age < 0) {
            System.out.println("Ошибка! Возраст не может быть отрицательным.");
        } else if (age < 12) {
            System.out.println("Категория: Ребёнок");
        } else if (age < 18) {
            System.out.println("Категория: Подросток");
        } else if (age < 60) {
            System.out.println("Категория: Взрослый");
        } else {
            System.out.println("Категория: Пенсионер");
        }
    }
    public void checkFreeTime(int hours, int mood) {
        System.out.println("\n=== ПРОВЕРКА СВОБОДНОГО ВРЕМЕНИ ===");

        if (hours >= 2 && mood == 1) {
            System.out.println("Рекомендуем сходить в кино!");
        } else {
            System.out.println("Лучше выбрать занятие дома.");
        }
    }
    public void checkActivity(int hours, int mood) {
        System.out.println("\n=== ПРОВЕРКА АКТИВНОСТИ ===");

        if (hours >= 3 || mood == 3) {
            System.out.println("Рекомендуем активное развлечение!");
        } else {
            System.out.println("Рекомендуем спокойный отдых.");
        }
    }
    public void calculateDiscount(double purchase, boolean premium) {
        System.out.println("\n=== СИСТЕМА СКИДОК ===");

        double discount = 0;

        if (purchase >= 10000 && premium) {
            discount = 20;
        } else if (purchase >= 10000) {
            discount = 10;
        } else if (premium) {
            discount = 5;
        }

        double result = purchase - (purchase * discount / 100);

        System.out.println("Сумма покупки: " + purchase);
        System.out.println("Скидка: " + discount + "%");
        System.out.println("Итоговая сумма: " + result);
    }
    public void checkFinance(double income, double expenses) {
        System.out.println("\n=== ФИНАНСОВЫЙ АНАЛИЗ ===");

        double balance = income - expenses;

        System.out.println("Доходы: " + income);
        System.out.println("Расходы: " + expenses);
        System.out.println("Остаток: " + balance);

        if (balance > 0) {
            System.out.println("Финансовое состояние: Хорошее");
        } else if (balance == 0) {
            System.out.println("Финансовое состояние: Стабильное");
        } else {
            System.out.println("Финансовое состояние: Проблемное");
        }
    }
    public void checkScore(int score) {
        System.out.println("\n=== СИСТЕМА ОЦЕНОК ===");

        if (score < 0 || score > 100) {
            System.out.println("Ошибка! Баллы должны быть от 0 до 100.");
        } else if (score >= 90) {
            System.out.println("Оценка: Отлично!");
        } else if (score >= 70) {
            System.out.println("Оценка: Хорошо!");
        } else if (score >= 50) {
            System.out.println("Оценка: Удовлетворительно.");
        } else {
            System.out.println("Результат: Не пройдено.");
        }
    }
    public void checkSafety(int age, int hours, boolean blocked) {
        System.out.println("\n=== ПРОВЕРКА БЕЗОПАСНОСТИ ===");

        if (age < 0 || hours < 0) {
            System.out.println("Ошибка введённых данных!");
        } else if (blocked) {
            System.out.println("Высокий уровень риска!");
        } else if (age < 18 && hours > 8) {
            System.out.println("Средний уровень риска!");
        } else {
            System.out.println("Низкий уровень риска!");
        }
    }
    public void giveRecommendation(int mood, int budget) {
        System.out.println("\n=== ВАША РЕКОМЕНДАЦИЯ ===");

        if (budget < 0) {
            System.out.println("Ошибка бюджета!");
        } else if (mood == 1 && budget >= 5000) {
            System.out.println("Рекомендуем сходить в кино или кафе!");
        } else if (mood == 2) {
            System.out.println("Рекомендуем отдохнуть дома и посмотреть фильм!");
        } else if (mood == 3 && budget < 5000) {
            System.out.println("Рекомендуем погулять или порисовать!");
        } else if (mood >= 1 && mood <= 3) {
            System.out.println("Рекомендуем послушать музыку!");
        } else {
            System.out.println("Ошибка! Неизвестное настроение.");
        }
    }
    public void calculateService(int tariff, int extra) {
        System.out.println("\n=== РАСЧЁТ СТОИМОСТИ ===");

        int price;
        switch (tariff) {
            case 1:
                price = 1000;
                break;
            case 2:
                price = 2000;
                break;
            case 3:
                price = 3000;
                break;
            default:
                System.out.println("Неверный тариф!");
                return;
        }

        if (extra == 1) {
            price += 500;
        } else if (extra != 0) {
            System.out.println("Неверный выбор дополнительной услуги!");
            return;
        }

        System.out.println("Стоимость услуги: " + price + " тенге");
    }
    public void makeDecision(int age, int hours, int mood, int budget) {
        System.out.println("\n=== SMART DECISION ===");

        if (age < 0 || hours < 0 || budget < 0) {
            System.out.println("Ошибка! Проверьте введённые данные.");
        } else if (mood < 1 || mood > 3) {
            System.out.println("Ошибка! Неизвестное настроение.");
        } else if (age < 18 && hours >= 2 && mood == 1) {
            System.out.println("Решение: Посмотреть фильм или поиграть.");
        } else if (age >= 18 && hours >= 3 && budget >= 5000) {
            System.out.println("Решение: Сходить в кино или кафе.");
        } else if (mood == 2 || hours < 2) {
            System.out.println("Решение: Отдохнуть дома.");
        } else {
            System.out.println("Решение: Погулять или послушать музыку.");
        }
    }
    public void nestedCheck(int age, int hours, int mood) {
        System.out.println("\n=== ДОПОЛНИТЕЛЬНАЯ ПРОВЕРКА ===");

        if (age >= 12) {
            if (hours >= 2) {
                if (mood == 1) {
                    System.out.println("Рекомендуем активное развлечение!");
                } else {
                    System.out.println("Рекомендуем посмотреть фильм.");
                }
            } else {
                System.out.println("Мало свободного времени. Послушайте музыку.");
            }
        } else {
            System.out.println("Рекомендуем детские развлечения.");
        }
    }

    public void analyzeUser(int age, int hours, int mood, int budget, int energy) {
        System.out.println("\n=== АНАЛИЗ ПОЛЬЗОВАТЕЛЯ ===");

        if (age < 0 || hours < 0 || budget < 0
                || mood < 1 || mood > 3
                || energy < 1 || energy > 10) {
            System.out.println("Ошибка! Проверьте введённые данные.");
            return;
        }

        int points = 0;
        if (age >= 18) {
            points++;
        }
        if (hours >= 3) {
            points++;
        }
        if (mood == 1) {
            points++;
        }
        if (budget >= 3000) {
            points++;
        }
        if (energy >= 7) {
            points++;
        }

        String level;
        String recommendation;

        if (points >= 4) {
            level = "Высокий";
            recommendation = "Займитесь активным развлечением!";

        } else if (points >= 2) {
            level = "Средний";
            recommendation = "Посмотрите фильм или поиграйте.";

        } else {
            level = "Низкий";
            recommendation = "Отдохните, послушайте музыку.";
        }

        Report report = new Report();

        report.createReport(level, recommendation, points);
    }

    public void login() {
        System.out.println("\n=== АВТОРИЗАЦИЯ ===");

        System.out.print("Введите логин: ");
        String username = scanner.next();

        System.out.print("Введите пароль: ");
        String password = scanner.next();

        if (username.equals("admin") && password.equals("1234")) {
            System.out.println("Авторизация успешна!");
        } else {
            System.out.println("Неверный логин или пароль!");
        }
    }
}