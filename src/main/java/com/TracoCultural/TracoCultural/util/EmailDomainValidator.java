package com.TracoCultural.TracoCultural.util;

import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.InitialDirContext;
import java.util.Hashtable;

public class EmailDomainValidator {

    private EmailDomainValidator() {}

    public static boolean dominioValido(String email) {
        if (email == null || !email.contains("@")) return false;
        String dominio = email.substring(email.lastIndexOf('@') + 1);
        if (dominio.isBlank()) return false;

        Hashtable<String, String> env = new Hashtable<>();
        env.put("java.naming.factory.initial",     "com.sun.jndi.dns.DnsContextFactory");
        env.put("java.naming.provider.url",         "dns:");
        env.put("com.sun.jndi.dns.timeout.initial", "2000");
        env.put("com.sun.jndi.dns.timeout.retries", "1");

        try {
            InitialDirContext ctx = new InitialDirContext(env);
            try {
                Attributes mx = ctx.getAttributes("dns:/" + dominio, new String[]{"MX"});
                if (mx.get("MX") != null) return true;

                Attributes a = ctx.getAttributes("dns:/" + dominio, new String[]{"A"});
                return a.get("A") != null;
            } finally {
                ctx.close();
            }
        } catch (NamingException e) {
            return false;
        }
    }
}
