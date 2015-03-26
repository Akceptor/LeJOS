import lejos.hardware.Button;
import lejos.hardware.Sound;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.port.MotorPort;
import lejos.hardware.port.Port;
import lejos.hardware.sensor.EV3IRSensor;
import lejos.hardware.sensor.SensorModes;
import lejos.robotics.RegulatedMotor;

public class RCRobot {

	@SuppressWarnings("resource")
	public static void main(String[] args) {
		// Speed
		boolean hiSpeed = false;
		// Motors
		RegulatedMotor m1 = new EV3LargeRegulatedMotor(MotorPort.A);
		RegulatedMotor m2 = new EV3LargeRegulatedMotor(MotorPort.B);
		m1.setSpeed(200);
		m2.setSpeed(200);
		m2.synchronizeWith(new RegulatedMotor[] { m1 });
		m2.startSynchronization();
		// IR Remote
		Port port = LocalEV3.get().getPort("S1");
		SensorModes sensor = new EV3IRSensor(port);
		System.out.println("Press any key to start");
		Button.waitForAnyPress();
		System.out.println("Press ESCAPE key to stop");
		int keys = 0;
		while (keys != 32 /* ESCAPE */) {
			// Acquire remote channel 1
			int command = ((EV3IRSensor) sensor).getRemoteCommand(0);
			switch (command) {
			case 1:
				// Left fwd
				m2.forward();
				break;
			case 2:
				// left bwd
				m2.backward();
				break;
			case 3:
				// right fwd
				m1.forward();
				break;
			case 4:
				// right bwd
				m1.backward();
				break;
			case 5:
				// both fwd
				m2.startSynchronization();
				m1.forward();
				m2.forward();
				m2.endSynchronization();
				break;
			case 6:
				// rotate left
				m2.startSynchronization();
				m1.backward();
				m2.forward();
				m2.endSynchronization();
				break;
			case 7:
				// rotate right
				m2.startSynchronization();
				m1.forward();
				m2.backward();
				m2.endSynchronization();
				break;
			case 8:
				// both bwd
				m2.startSynchronization();
				m1.backward();
				m2.backward();
				m2.endSynchronization();
				break;
			case 9: // PRESS AND RELEASE!!!
				// speed change
				if (hiSpeed) {
					m1.setSpeed(400);
					m2.setSpeed(400);
					Sound.playTone(2000, 1000);
				} else {
					m1.setSpeed(200);
					m2.setSpeed(200);
					Sound.playTone(200, 1000);
				}

			default:
				// stop
				m2.startSynchronization();
				m1.stop();
				m2.stop();
				m2.endSynchronization();
			}
			// Acquire keys
			keys = Button.readButtons();
		}

	}
}
