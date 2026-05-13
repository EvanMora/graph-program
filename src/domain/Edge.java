package domain;

public class Edge{
    private Node origen;
    private Node destino;
    private double peso;

    public Edge(Node origen , Node destino , double peso){
        this.origen=origen;
        this.destino=destino;
        this.peso=peso;
    }
    //override 
    @Override
    public String toString() {
        return origen + "->" + destino + ":" + peso + "Km";
    }
    // getter accede 
    //setter modifica 
        //getter origen 
    public Node getOrigen(){
        return origen;
    }  
        //getter Destino
    public Node getDestino(){
        return destino;
    }
        //getter Peso 
    public double getPeso(){
        return peso;
    }
        //setter origen  
    public void setOrigen(Node origen){
        this.origen = origen; 
    }
        //setter destino
    public void setDestino(Node destino){
        this.destino = destino;

    }
        //setter peso
    public void setPeso(double peso){
        this.peso = peso ;

    }   
}
