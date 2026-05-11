public class Nodo {
    private int id;
    private String nombre;
    private int x;
    private int y; 

    public Nodo(int id, String nombre, int x, int y){
        this.id = id;
        this.nombre = nombre;
        this.x = x;
        this.y = y;
    }
    @Override
    //to string 
    //uso de lower camel case (mayusculas y minusculas)
    public String toString() {
        return nombre;
    }
    // getter accede 
    //setter modifica 
        //getter id
    public int getId(){
        return id;
    }  
        //getter nombre
    public String getNombre(){
        return nombre;
    }
        //getter x
    public int getX(){
        return x;
    }
        //getter  y
    public int getY(){
        return y;
    }
        //setter id 
    public void setId(int id){
        this.id = id; 

    }
        //setter nombre
    public void setNombre(String nombre){
        this.nombre = nombre;

    }
        //setter x
    public void setX(int x){
        this.x = x ;

    }
    //setter y
    public void setY(int y){
        this.y = y ;

    }

}
