package com.sleddgang.gameStackClient;

import java.util.Scanner;
import java.util.concurrent.ThreadLocalRandom;
//import java.util.Random;

public class Client {
    public static void main(String[] args) {

        int userInput;
        int botInput;
        String gameEnd;
        boolean gameStatus = true;
        //Random rand = new Random();



        Scanner keyboard = new Scanner(System.in);

        while (gameStatus) {
            System.out.print("\n\n==================================================\n" +
                             "Enter your attack" +
                             "\n1 = rock, 2 = paper, 3 = scissors, 4 = end program" +
                             "\n==================================================\n");
            userInput = keyboard.nextInt();

            switch (userInput) {
                case 1:
                    System.out.println("You used Rock");
                    break;
                case 2:
                    System.out.println("You used Paper");
                    break;
                case 3:
                    System.out.println("You used Scissors");
                    break;
                case 4:
                    System.exit(0);
                default:
                    System.out.println("You're a dummie. Use one of the specified options");
                    continue;
            }


            botInput = ThreadLocalRandom.current().nextInt(0, 2 + 1)+1;
            //botInput = rand.nextInt(3) +1;

            switch (botInput) {
                case 1:
                    System.out.println("Your opponent used Rock\n");
                    break;
                case 2:
                    System.out.println("Your opponent used Paper\n");
                    break;
                case 3:
                    System.out.println("Your opponent used Scissors\n");
                    break;
            }


            if (userInput == botInput) {
                gameEnd = "tie";


            } else if ((botInput == 1 && userInput == 2) || (botInput == 2 && userInput == 3) || (botInput == 3 && userInput == 1)) {
                gameEnd = "win";

            } else {
                gameEnd = "lose";
            }


//        } else if ((botInput == 1 && userInput == 3) || (botInput == 2 && userInput == 1) || (botInput == 3 && userInput == 2)) {
//            gameEnd = "lose";
//        }

            System.out.println(gameEnd);


        }
    }


}
