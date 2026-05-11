public class Arista{
    private Nodo origen;
    private Nodo destino;
    private double peso;

    public Arista(Nodo origen , Nodo destino , double peso){
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
    public Nodo getOrigen(){
        return origen;
    }  
        //getter Destino
    public Nodo getDestino(){
        return destino;
    }
        //getter Peso 
    public double getPeso(){
        return peso;
    }
        //setter origen  
    public void setOrigen(Nodo origen){
        this.origen = origen; 
    }
        //setter destino
    public void setDestino(Nodo destino){
        this.destino = destino;

    }
        //setter peso
    public void setPeso(double peso){
        this.peso = peso ;

    }   
}
