package com.minimine.utils.blocos;

public class BlocoTipo {
    public final int idTopo;
    public final int idLado;
    public final int idBaixo;
    public final boolean transparente;
    public final boolean solido;

    public BlocoTipo(int idTopo, int idLado, int idBaixo, boolean transparente, boolean solido) {
        this.idTopo = idTopo;
        this.idLado = idLado;
        this.idBaixo = idBaixo;
        this.transparente = transparente;
        this.solido = solido;
    }

    public static BlocoTipo criar(int tipo) {
        switch(tipo) {
            case 1: return new BlocoTipo(0, 1, 2, false, true);  // grama
            case 2: return new BlocoTipo(2, 2, 2, false, true);  // terra
            case 3: return new BlocoTipo(3, 3, 3, false, true);  // pedra
            case 4: return new BlocoTipo(4, 4, 4, false, true);  // agua(transparente)
            case 5: return new BlocoTipo(5, 5, 5, false, true);  // areia
            case 6: return new BlocoTipo(6, 7, 6, false, true);  // tronco
            case 7: return new BlocoTipo(8, 8, 8, true, false);  // folha(transparente)
            default: return null;
        }
    }

    public int textureId(int faceId) {
        switch(faceId) {
            case 0: return idTopo;   // topo
            case 1: return idBaixo;  // baixo
            default: return idLado;  // lados
        }
    }
}
