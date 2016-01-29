package com.blogspot.tsprates.pso;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

public class Particula {

    private final List<List<String>> pbest = new ArrayList<List<String>>();

    private List<String> pos = new ArrayList<String>();

    private String classe;

    public Particula(List<String> vel, List<String> pos, String classe) {
        this.pos = pos;
        this.pbest.add(pos);
        this.classe = classe;
    }


    public List<String> getPosicao() {
        return pos;
    }

    public void addpbest(List<String> pbest) {
        this.pbest.add(pos);
    }

    public String getWhereSql() {
        return joinArray(pos);
    }

    public String joinArray(List<String> l) {
        return "(" + StringUtils.join(l, ") AND (") + ")";
    }

    public String getClasse() {
        return this.classe;
    }
    
    public int getSize() {
        return this.pos.size();
    }

}
