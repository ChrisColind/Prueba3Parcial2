/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package binarios;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Calendar;
import java.util.Date;

/**
 *
 * @author Rogelio
 */
public class EmpleadoManager {
    private RandomAccessFile rcods, remps;
    
    public EmpleadoManager(){
        try{
            File mf = new File("company");
            mf.mkdir();
            
            rcods = new RandomAccessFile("company/codigo.emp","rw");
            remps = new RandomAccessFile("company/empleado.emp","rw");
            
            initCodes();
            
        }catch(IOException e){
            System.out.println(e.getMessage());
        }
    }
    
    private void initCodes()throws IOException{
        if(rcods.length()==0){
            rcods.seek(0);
            rcods.writeInt(1);
        }
    }
    
    private int getCode()throws IOException{
        rcods.seek(0);
        int code = rcods.readInt();
        rcods.seek(0);
        rcods.writeInt(code + 1);
        return code;
    }
    
    public void addEmployee(String name, double salary)throws IOException{
        remps.seek(remps.length());
        int code = getCode();
        remps.writeInt(code); //+4
        remps.writeUTF(name); //+2n
        remps.writeDouble(salary);//+8
        remps.writeLong(Calendar.getInstance().getTimeInMillis());
        remps.writeLong(0); 
        createEmployeeFolder(code);
        System.out.println("Empleado "+name+" agregado con codigo "+code);
        //crear folder
        /*
        28
        Cristopher
        30,000
        9/6/2026
        0
        */
    }
    
    private String employeeFolder(int code){
        return "company/empleado"+code;
    }
    
    private RandomAccessFile salesFilefor(int code)throws IOException{
        String dirPadre =employeeFolder(code);
        int yearActual = Calendar.getInstance().get(Calendar.YEAR);
        String path = dirPadre+"/ventas"+yearActual+".emp";
        return new RandomAccessFile(path,"rw");
    }
    
    private RandomAccessFile billsFilefor(int code)throws IOException{
        String dirPadre = employeeFolder(code);
        String path = dirPadre+"/recibos.emp";
        return new RandomAccessFile(path,"rw");
    }
    
    private void createSaleFileFor(int code)throws IOException{
        RandomAccessFile ryear = salesFilefor(code);
        if(ryear.length()==0){
            for(int mes=0 ; mes<12 ; mes++){
                ryear.writeDouble(0);
                ryear.writeBoolean(false);
            }
        }
        ryear.close();
    }
    
    private void createEmployeeFolder(int code)throws IOException{
        File edir = new File(employeeFolder(code));
        edir.mkdir();
        createSaleFileFor(code);
    }
    
    public void employeeList()throws IOException{
        if(remps.length()==0){
            System.out.println("No hay empleados registrados");
            return;
        }
        boolean hayActivos = false;
        remps.seek(0);
        while(remps.getFilePointer()<remps.length()){
            int code = remps.readInt();
            String name = remps.readUTF();
            double sal = remps.readDouble();
            Date fecha = new Date(remps.readLong());
            if(remps.readLong()==0){
                System.out.println(code+"-"+name+"-Lps."+sal+" contrato el: "+fecha);
                hayActivos = true;
            }
        }
        if(!hayActivos){
            System.out.println("No hay empleados activos");
        }
    }
    
    private boolean isEmployeeActive(int code)throws IOException{
        remps.seek(0);
        while(remps.getFilePointer()<remps.length()){
            int codeI = remps.readInt();
            long pos = remps.getFilePointer();
            remps.readUTF();
            remps.skipBytes(16);
            if(remps.readLong()==0 && codeI==code){
                remps.seek(pos);
                return true;
            }
        }
        return false;
    }
    
    public boolean fireEmployee(int code)throws IOException{
        
        if(isEmployeeActive(code)){
            String name = remps.readUTF();
            remps.skipBytes(16);
            remps.writeLong(new Date().getTime());
            System.out.println("Empleado "+name+" despedido exitosamente");
            return true;
        }
        System.out.println("No se pudo despedir, empleado no encontrado o ya inactivo");
        return false;
    }
    
    public void addSaleToEmployee(int code, double monto)throws IOException{
        
        if(!isEmployeeActive(code)){
            System.out.println("Empleado no encontrado o inactivo");
            return;
        }
        
        RandomAccessFile rventas = salesFilefor(code);
        int mesActual = Calendar.getInstance().get(Calendar.MONTH);
        long pos = (long) mesActual * 9;
        rventas.seek(pos);
        double ventasActuales = rventas.readDouble();
        rventas.seek(pos);
        rventas.writeDouble(ventasActuales + monto);
        rventas.close();
    }
    
    public boolean isEmployeePayed(int code)throws IOException{
        RandomAccessFile rventas = salesFilefor(code);
        int mesActual = Calendar.getInstance().get(Calendar.MONTH);
        long pos = (long) mesActual * 9;
        rventas.seek(pos);
        rventas.skipBytes(8);
        boolean pagado = rventas.readBoolean();
        rventas.close();
        return pagado;
    }
    
    public void payEmployee(int code)throws IOException{
        if(!isEmployeeActive(code) || isEmployeePayed(code)){
            System.out.println("No se pudo pagar");
            return;
        }
        
        int yearActual = Calendar.getInstance().get(Calendar.YEAR);
        int mesActual = Calendar.getInstance().get(Calendar.MONTH);
        
        RandomAccessFile rventas = salesFilefor(code);
        long posVentas = (long) mesActual * 9;
        rventas.seek(posVentas);
        double ventas = rventas.readDouble();
        
        String name = remps.readUTF();
        double salario = remps.readDouble();
        
        double sueldo = salario + (ventas * 0.10);
        double deduccion = sueldo * 0.035;
        double total = sueldo - deduccion;
        
        RandomAccessFile rrecibos = billsFilefor(code);
        rrecibos.seek(rrecibos.length());
        rrecibos.writeLong(Calendar.getInstance().getTimeInMillis());
        rrecibos.writeDouble(sueldo);
        rrecibos.writeDouble(deduccion);
        rrecibos.writeInt(yearActual);
        rrecibos.writeInt(mesActual);
        rrecibos.close();
        
        rventas.seek(posVentas + 8);
        rventas.writeBoolean(true);
        rventas.close();
        
        System.out.println("Empleado "+name+" se le pago Lps. "+total);
    }
    
    public void printEmployee(int code)throws IOException{
        if(!isEmployeeActive(code)){
            System.out.println("Empleado no encontrado");
            return;
        }
        
        String name = remps.readUTF();
        double salario = remps.readDouble();
        Date fechaContrato = new Date(remps.readLong());
        
        System.out.println("Codigo: "+code+" Nombre: "+name+" Salario: "+salario+" Fecha de contratacion: "+fechaContrato);
        
        RandomAccessFile rventas = salesFilefor(code);
        rventas.seek(0);
        double totalAnual = 0;
        for(int mes=0 ; mes<12 ; mes++){
            double ventasMes = rventas.readDouble();
            rventas.skipBytes(1);
            totalAnual += ventasMes;
            System.out.println("Mes "+(mes+1)+" : "+ventasMes);
        }
        rventas.close();
        System.out.println("Total de ventas del año: "+totalAnual);
        
        RandomAccessFile rrecibos = billsFilefor(code);
        int totalRecibos = 0;
        rrecibos.seek(0);
        //L8 +D8 + D8+ I4 + I4 = 32
        
        while(rrecibos.getFilePointer()<rrecibos.length()){
            rrecibos.skipBytes(32);
            totalRecibos++;
        }
        rrecibos.close();
        System.out.println("Total de pagos realizados: "+totalRecibos);
    }
}
