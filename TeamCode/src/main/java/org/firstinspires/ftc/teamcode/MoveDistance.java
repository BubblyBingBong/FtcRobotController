package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;

@Autonomous
public class MoveDistance extends LinearOpMode {
    DcMotor fLeft;
    DcMotor fRight;
    DcMotor bLeft;
    DcMotor bRight;

    @Override
    public void runOpMode() throws InterruptedException {
        fLeft = hardwareMap.dcMotor.get("fLeft");
        fRight = hardwareMap.dcMotor.get("fRight");
        bLeft = hardwareMap.dcMotor.get("bLeft");
        bRight = hardwareMap.dcMotor.get("bRight");

        fLeft.setDirection(DcMotorSimple.Direction.FORWARD);
        fRight.setDirection(DcMotorSimple.Direction.REVERSE);
        bLeft.setDirection(DcMotorSimple.Direction.FORWARD);
        bRight.setDirection(DcMotorSimple.Direction.REVERSE);

        fLeft.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        fRight.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        bLeft.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        bRight.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);

        fLeft.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        fRight.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        bLeft.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        bRight.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);

        waitForStart();

        //delay so you have time to look at the robot before its over
        sleep(3000);

        //4 feet is 66200 ticks
        //left is fLeft
        //right is fRight
        //back is bLeft

        //1 foot is 16550
        double inches = 48;

        double tickDistance = (inches/12) * 16550;
        double P_value = tickDistance;
        double I_value = 0;

        double P_power;
        double I_power = 0;

        //under .001 second a loop
        while (opModeIsActive()){
            P_value = tickDistance + ((fLeft.getCurrentPosition() + fRight.getCurrentPosition())/2);
            P_power = 0.75 * P_value/66200;

            I_value += P_value;
            I_power = I_value/10000000;

            telemetry.addData("left encoder ticks", fLeft.getCurrentPosition());
            telemetry.addData("right encoder ticks", fRight.getCurrentPosition());
            telemetry.addData("P value (error)", P_value);
            telemetry.addData("I value (sum)", I_value);
            telemetry.addData("I power", I_power);
            telemetry.update();

            fLeft.setPower(P_power + I_power);
            fRight.setPower(P_power + I_power);
            bLeft.setPower(P_power + I_power);
            bRight.setPower(P_power + I_power);
        }
        fLeft.setPower(0);
        fRight.setPower(0);
        bLeft.setPower(0);
        bRight.setPower(0);

        sleep(10000);
    }
}
