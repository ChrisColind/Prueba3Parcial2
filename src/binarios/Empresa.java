/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package binarios;

import java.io.IOException;
import java.util.Scanner;

/**
 *
 * @author Rogelio
 */
public class Empresa {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args){
    Scanner lea = new Scanner(System.in);
    EmpleadoManager em = new EmpleadoManager();
    int opcion = 0;
    try{
        do{
            System.out.println("\n\nMenu\n");
            System.out.println("1.- Agregar empleado");
            System.out.println("2.- Listar empleados no despedidos");
            System.out.println("3.- Agregar venta a empleado");
            System.out.println("4.- Pagar empleado");
            System.out.println("5.- Despedir empleado");
            System.out.println("6.- Imprimir empleado");
            System.out.println("7.- Salir");
            System.out.print("Escoja una opcion: ");
            opcion = lea.nextInt();
            switch(opcion){
                case 1:
                    System.out.print("Nombre: ");
                    String nombre = lea.next();
                    System.out.print("Salario: ");
                    double salario = lea.nextDouble();
                    em.addEmployee(nombre, salario);
                break;
                
                case 2:
                    em.employeeList();
                break;
                
                case 3:
                    System.out.print("Codigo: ");
                    int codVenta = lea.nextInt();
                    System.out.print("Monto: ");
                    double monto = lea.nextDouble();
                    em.addSaleToEmployee(codVenta, monto);
                break;
                    
                case 4:
                    System.out.print("Ingrese el codigo: ");
                    int codPago = lea.nextInt();
                    em.payEmployee(codPago);
                break;
                
                case 5:
                    System.out.print("Codigo: ");
                    int codDespido = lea.nextInt();
                    System.out.print("Esta seguro que desea despedir al empleado? (s/n): ");
                    String confirmar = lea.next();
                    if(confirmar.equals("s")){
                        em.fireEmployee(codDespido);
                    }else{
                        System.out.println("No se despidio al empleado");
                    }
                break;
                
                case 6:
                    System.out.print("Ingrese el codigo: ");
                    int codPrint = lea.nextInt();
                    em.printEmployee(codPrint);
                break;
                
            }
        }while(opcion!=7);
            }catch(IOException e){
                System.out.println(e.getMessage());
            }
    }
}

