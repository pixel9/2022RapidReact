package frc.robot.commands.autonomous;

import edu.wpi.first.wpilibj2.command.CommandBase;
import frc.robot.subsystems.DriveTrain;


public class AutonDrive extends CommandBase{
    DriveTrain driveTrain;
    Boolean finish;

    public AutonDrive(DriveTrain d) {
        driveTrain = d;
        addRequirements(driveTrain);
    }

    @Override
	public void initialize() {
		System.out.println("Running DriveAuton");
		finish = false;
    }


    @Override
	public void execute() {
		if (Math.abs(driveTrain.getLeftEncoder().getPosition()) <= 1) {
			driveTrain.arcadeDrive(.5, 0);
		}
		else {
			driveTrain.stop();
			finish = true;
		}
	}
    
