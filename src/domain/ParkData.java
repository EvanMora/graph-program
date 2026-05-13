package domain;

public class ParkData {
    public Node[] parkArray = {
        new Node(0, "UMB", 200, 80),
        new Node(1, "Parque Nacional", 300, 120),
        new Node(2, "Parque de la 93", 50, 150),
        new Node(3, "Parque Simon Bolivar", 180, 300),
        new Node(4, "Parque el Virrey", 220, 450),
        new Node(5, "Parque el Tunal", 350, 400),
        new Node(6, "Timiza", 420, 300),
        new Node(7, "Mundo aventura", 420, 150)
    };
    public Graph parkGraph = new Graph(parkArray); 

    public ParkData() {
        parkGraph.setConnection(parkArray[0], parkArray[1], 3.4);
        parkGraph.setConnection(parkArray[0], parkArray[2], 5.2);
        parkGraph.setConnection(parkArray[3], parkArray[4], 7.2);
        parkGraph.setConnection(parkArray[3], parkArray[1], 7);
        parkGraph.setConnection(parkArray[3], parkArray[2], 7.8);
        parkGraph.setConnection(parkArray[1], parkArray[2], 7.5);
        parkGraph.setConnection(parkArray[4], parkArray[2], 1.7);
        parkGraph.setConnection(parkArray[5], parkArray[6], 6.2);
        parkGraph.setConnection(parkArray[5], parkArray[0], 16);
        parkGraph.setConnection(parkArray[6], parkArray[7], 3.9);
    }
}
