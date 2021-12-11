package br.com.fiap.scj.camelexample.services;

import br.com.fiap.scj.camelexample.beans.MyBean;

import java.util.Random;

public class MyBeanService {
    public static void example(MyBean bean){
        bean.setName("Hello, meu primeiro método camel");
        bean.setId(new Random().nextInt());
    }
}
