-- Execute no SQL Editor do Supabase UMA vez, se o banco já existia antes desta versão.
-- O Hibernate cria um CHECK na coluna de enum e, com ddl-auto=update, não o atualiza;
-- sem isto, gravar o status CONTRATADO falha com "violates check constraint".
DO $$
DECLARE c text;
BEGIN
  FOR c IN
    SELECT conname FROM pg_constraint
    WHERE conrelid = 'public.alugueis'::regclass
      AND contype = 'c'
      AND pg_get_constraintdef(oid) ILIKE '%status%'
  LOOP
    EXECUTE format('ALTER TABLE public.alugueis DROP CONSTRAINT %I', c);
  END LOOP;
END $$;

ALTER TABLE public.alugueis
  ADD CONSTRAINT alugueis_status_check
  CHECK (status IN ('PENDENTE','EM_ANALISE','APROVADO','REJEITADO','CONTRATADO','CANCELADO'));
