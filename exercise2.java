import java.time.LocalTime;
import java.util.*;

// =====================================================
// ---------- Device Classes ----------
// =====================================================
abstract class Device {
    protected int id;
    protected String type;

    public Device(int deviceId, String deviceType) {
        this.id = deviceId;
        this.type = deviceType;
    }

    public int getId() { return id; }
    public String getType() { return type; }

    public abstract String statusReport();
}

class Light extends Device {
    private String power = "off";

    public Light(int id) {
        super(id, "light");
    }

    public void turnOn() { power = "on"; }
    public void turnOff() { power = "off"; }
    public String statusReport() {
        return "Light " + id + " is " + power;
    }
}

class Thermostat extends Device {
    private int temperature = 70;

    public Thermostat(int id) {
        super(id, "thermostat");
    }

    public void setTemperature(int temp) { temperature = temp; }
    public String statusReport() {
        return "Thermostat " + id + " at " + temperature + "°F";
    }
}

class DoorLock extends Device {
    private String lock = "locked";

    public DoorLock(int id) {
        super(id, "door");
    }

    public void lockDoor() { lock = "locked"; }
    public void unlockDoor() { lock = "unlocked"; }
    public String statusReport() {
        return "Door " + id + " is " + lock;
    }
}


// =====================================================
// ---------- Factory ----------
// =====================================================
class DeviceFactory {
    public static Device create(int deviceId, String deviceType) {
        switch (deviceType.toLowerCase()) {
            case "light": return new Light(deviceId);
            case "thermostat": return new Thermostat(deviceId);
            case "door": return new DoorLock(deviceId);
            default: throw new IllegalArgumentException("Unknown device type");
        }
    }
}


// =====================================================
// ---------- Proxy ----------
// =====================================================
class DeviceProxy {
    private Device device;
    private String token; // "admin" or "public"

    public DeviceProxy(Device device, String token) {
        this.device = device;
        this.token = token;
    }

    public void call(String method, Object... args) {
        // simple auth: admin can do all, public only lights
        if (!token.equals("admin") && !device.getType().equals("light")) {
            System.out.println("❌ Unauthorized");
            return;
        }
        try {
            Class<?>[] paramTypes = Arrays.stream(args).map(Object::getClass).toArray(Class[]::new);
            device.getClass().getMethod(method, paramTypes).invoke(device, args);
        } catch (Exception e) {
            System.out.println("⚠️ Error calling method: " + e.getMessage());
        }
    }

    public String statusReport() {
        return device.statusReport();
    }
}


// =====================================================
// ---------- Observer ----------
// =====================================================
interface HubObserver {
    void update(String event, Map<String, Object> payload);
}

class ConsoleObserver implements HubObserver {
    @Override
    public void update(String event, Map<String, Object> payload) {
        System.out.println("[EVENT] " + event + ": " + payload);
    }
}


// =====================================================
// ---------- Hub ----------
// =====================================================
class Hub {
    private Map<Integer, DeviceProxy> devices = new HashMap<>();
    private List<HubObserver> observers = new ArrayList<>();
    private List<Schedule> schedules = new ArrayList<>();

    public void addObserver(HubObserver obs) {
        observers.add(obs);
    }

    private void notifyAllObservers(String event, Map<String, Object> payload) {
        for (HubObserver o : observers) o.update(event, payload);
    }

    public void addDevice(Device device, String token) {
        devices.put(device.getId(), new DeviceProxy(device, token));
        notifyAllObservers("device_added", Map.of("id", device.getId(), "type", device.getType()));
    }

    public void turnOn(int id) {
        devices.get(id).call("turnOn");
        notifyAllObservers("turn_on", Map.of("id", id));
    }

    public void turnOff(int id) {
        devices.get(id).call("turnOff");
        notifyAllObservers("turn_off", Map.of("id", id));
    }

    public void setTemp(int id, int temp) {
        devices.get(id).call("setTemperature", temp);
        notifyAllObservers("set_temp", Map.of("id", id, "temp", temp));
    }

    public void unlock(int id) {
        devices.get(id).call("unlockDoor");
        notifyAllObservers("unlock", Map.of("id", id));
    }

    public void lock(int id) {
        devices.get(id).call("lockDoor");
        notifyAllObservers("lock", Map.of("id", id));
    }

    public void status() {
        for (DeviceProxy proxy : devices.values()) {
            System.out.println(proxy.statusReport());
        }
    }

    // ---------- Scheduler ----------
    private static class Schedule {
        int id;
        String time;
        String command;

        Schedule(int id, String time, String command) {
            this.id = id;
            this.time = time;
            this.command = command;
        }
    }

    public void setSchedule(int id, String time, String command) {
        schedules.add(new Schedule(id, time, command));
        notifyAllObservers("schedule_added", Map.of("id", id, "time", time, "cmd", command));
    }

    public void runPending(String now) {
        for (Schedule s : schedules) {
            if (s.time.equals(now)) {
                switch (s.command) {
                    case "turnOn": turnOn(s.id); break;
                    case "turnOff": turnOff(s.id); break;
                }
            }
        }
    }
}


// =====================================================
// ---------- Demo ----------
// =====================================================
public class SmartHome {
    public static void main(String[] args) {
        Hub hub = new Hub();
        hub.addObserver(new ConsoleObserver());

        Device d1 = DeviceFactory.create(1, "light");
        Device d2 = DeviceFactory.create(2, "thermostat");
        Device d3 = DeviceFactory.create(3, "door");

        hub.addDevice(d1, "public");
        hub.addDevice(d2, "admin");
        hub.addDevice(d3, "admin");

        System.out.println("\n--- Initial Status ---");
        hub.status();

        // commands
        hub.turnOn(1);
        hub.setTemp(2, 76);
        hub.unlock(3);

        System.out.println("\n--- After Commands ---");
        hub.status();

        // schedule
        String now = LocalTime.now().withSecond(0).withNano(0).toString().substring(0,5); // HH:MM
        hub.setSchedule(1, now, "turnOff");
        hub.runPending(now);

        System.out.println("\n--- After Schedule ---");
        hub.status();
    }
}
