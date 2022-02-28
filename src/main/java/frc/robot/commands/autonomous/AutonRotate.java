package frc.robot.commands.autonomous;

import edu.wpi.first.wpilibj2.command.CommandBase;
import frc.robot.Constants;
import frc.robot.subsystems.DriveTrain;

public class AutonRotate extends CommandBase{
    DriveTrain driveTrain;
    Boolean finish;

    public AutonRotate(DriveTrain d) {
        driveTrain = d;
        addRequirements(driveTrain);
    }

    @Override
	public void initialize() {
		System.out.println("Running AutonRotate");
		finish = false;
    }

	public void execute(double distance) {
		if (limelight finds target) {
			driveTrain.driveWithJoysticks(0, angletotarget);
		}
		else {
			driveTrain.stop();
			finish = true;
		}
	}
    
	@Override
	public void end(boolean interrupted) {
		driveTrain.stop();
		System.out.println("Stopping AutonRotate");
	}

	// Returns true when the command should end.
	@Override
	public boolean isFinished() {
		return finish;
	}
}
