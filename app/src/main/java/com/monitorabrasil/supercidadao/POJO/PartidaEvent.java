package com.monitorabrasil.supercidadao.POJO;

import com.parse.ParseObject;

/**
 * Created by geral_000 on 04/03/2016.
 */
public class PartidaEvent extends Event {


    private ParseObject partida;

    public PartidaEvent(String action, ParseObject object, String erro) {
        this.action = action;
        this.erro = erro;
        this.partida = object;
    }

    public PartidaEvent(String action) {
        this.action=action;
    }

    public ParseObject getPartida() {
        return partida;
    }

    public void setPartida(ParseObject partida) {
        this.partida = partida;
    }
}
