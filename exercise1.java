// =====================================================
// 1. BEHAVIORAL PATTERNS
// =====================================================

import java.util.*;

// ---------- Observer Pattern ----------
// Story: YouTube channel → subscribers get new video notification
interface Observer {
    void update(String video);
}

class Subscriber implements Observer {
    private String name;
    public Subscriber(String name) { this.name = name; }
    public void update(String video) {
        System.out.println(name + " notified: New video - " + video);
    }
}

class Channel {
    private List<Observer> subscribers = new ArrayList<>();
    public void subscribe(Observer sub) { subscribers.add(sub); }
    public void unsubscribe(Observer sub) { subscribers.remove(sub); }
    public void upload(String video) {
        for (Observer s : subscribers) {
            s.update(video);
        }
    }
}

class DemoObserver {
    static void run() {
        System.out.println("\n--- Observer Pattern ---");
        Channel yt = new Channel();
        Subscriber a = new Subscriber("Alice");
        Subscriber b = new Subscriber("Bob");
        yt.subscribe(a);
        yt.subscribe(b);
        yt.upload("Observer Pattern Tutorial");
    }
}


// ---------- Strategy Pattern ----------
// Story: Payment method flexible (Card / UPI)
interface PaymentStrategy {
    void pay(int amount);
}

class CardPayment implements PaymentStrategy {
    public void pay(int amount) {
        System.out.println("Paid " + amount + " with Card");
    }
}

class UPIPayment implements PaymentStrategy {
    public void pay(int amount) {
        System.out.println("Paid " + amount + " with UPI");
    }
}

class PaymentContext {
    private PaymentStrategy strategy;
    public PaymentContext(PaymentStrategy strategy) { this.strategy = strategy; }
    public void pay(int amount) { strategy.pay(amount); }
}

class DemoStrategy {
    static void run() {
        System.out.println("\n--- Strategy Pattern ---");
        new PaymentContext(new CardPayment()).pay(500);
        new PaymentContext(new UPIPayment()).pay(300);
    }
}


// =====================================================
// 2. CREATIONAL PATTERNS
// =====================================================

// ---------- Singleton Pattern ----------
// Story: Only one Logger instance
class Logger {
    private static Logger instance;
    private Logger() {}
    public static Logger getInstance() {
        if (instance == null) {
            instance = new Logger();
        }
        return instance;
    }
    public void log(String msg) {
        System.out.println("LOG: " + msg);
    }
}

class DemoSingleton {
    static void run() {
        System.out.println("\n--- Singleton Pattern ---");
        Logger l1 = Logger.getInstance();
        Logger l2 = Logger.getInstance();
        l1.log("App Started");
        System.out.println("Same instance? " + (l1 == l2));
    }
}


// ---------- Factory Pattern ----------
// Story: Coffee shop kitchen creates drinks
interface Drink {
    void serve();
}

class Tea implements Drink {
    public void serve() { System.out.println("Serving Tea"); }
}

class Coffee implements Drink {
    public void serve() { System.out.println("Serving Coffee"); }
}

class DrinkFactory {
    public static Drink create(String type) {
        if (type.equalsIgnoreCase("tea")) return new Tea();
        if (type.equalsIgnoreCase("coffee")) return new Coffee();
        return null;
    }
}

class DemoFactory {
    static void run() {
        System.out.println("\n--- Factory Pattern ---");
        Drink d1 = DrinkFactory.create("tea");
        Drink d2 = DrinkFactory.create("coffee");
        if (d1 != null) d1.serve();
        if (d2 != null) d2.serve();
    }
}


// =====================================================
// 3. STRUCTURAL PATTERNS
// =====================================================

// ---------- Adapter Pattern ----------
// Story: Old 3-pin charger adapted to USB
class OldCharger {
    public void plug3pin() {
        System.out.println("Charging with 3-pin plug");
    }
}

interface USBCharger {
    void charge();
}

class ChargerAdapter implements USBCharger {
    private OldCharger old;
    public ChargerAdapter(OldCharger old) { this.old = old; }
    public void charge() { old.plug3pin(); }
}

class DemoAdapter {
    static void run() {
        System.out.println("\n--- Adapter Pattern ---");
        OldCharger old = new OldCharger();
        USBCharger usb = new ChargerAdapter(old);
        usb.charge();
    }
}


// ---------- Decorator Pattern ----------
// Story: Coffee → add Milk → add Sugar
interface CoffeeBase {
    int cost();
}

class SimpleCoffee implements CoffeeBase {
    public int cost() { return 10; }
}

class MilkDecorator implements CoffeeBase {
    private CoffeeBase base;
    public MilkDecorator(CoffeeBase base) { this.base = base; }
    public int cost() { return base.cost() + 5; }
}

class SugarDecorator implements CoffeeBase {
    private CoffeeBase base;
    public SugarDecorator(CoffeeBase base) { this.base = base; }
    public int cost() { return base.cost() + 2; }
}

class DemoDecorator {
    static void run() {
        System.out.println("\n--- Decorator Pattern ---");
        CoffeeBase c = new SimpleCoffee();
        c = new MilkDecorator(c);
        c = new SugarDecorator(c);
        System.out.println("Total cost: " + c.cost());  // 10+5+2 = 17
    }
}


// =====================================================
// MAIN DEMO
// =====================================================
public class DesignPatternsDemo {
    public static void main(String[] args) {
        DemoObserver.run();
        DemoStrategy.run();
        DemoSingleton.run();
        DemoFactory.run();
        DemoAdapter.run();
        DemoDecorator.run();
    }
}
