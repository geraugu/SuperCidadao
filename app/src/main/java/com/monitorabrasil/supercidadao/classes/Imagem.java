package com.monitorabrasil.supercidadao.classes;

import android.widget.ImageView;

import com.monitorabrasil.supercidadao.application.AppController;
import com.parse.ParseObject;

/**
 * Created by geral_000 on 20/03/2016.
 */
public class Imagem {
    public static void getFotoPolitico (ParseObject politico, ImageView img){
        if(politico.getString("tipo").equals("c"))
            AppController.getInstance().getmImagemLoader().displayImage(AppController.URL_FOTO_DEPUTADO + politico.get("idCadastro") + ".jpg", img);
        else
            AppController.getInstance().getmImagemLoader().displayImage(AppController.URL_FOTO_SENADOR + politico.get("idCadastro") + ".jpg", img);


    }
}
