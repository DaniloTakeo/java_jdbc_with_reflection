package orm.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface UmParaMuitos {
    Class<?> entidadeAlvo();
    String chaveEstrangeira(); // exemplo: "aluno_id" na tabela filha
}