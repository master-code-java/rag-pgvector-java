# Mini RAG com Java + Spring Boot + PostgreSQL/pgvector + OpenAI

Demo simples para evento Java+AI. Backend expõe endpoints para:
- **/api/docs**: ingestão de texto + embedding em pgvector
- **/api/search**: busca semântica top-K
- **/api/ask**: RAG (recupera contexto e gera resposta citando fontes)

## 1) Banco com pgvector

```bash
docker compose up -d
```

Cria `ragdb` com tabela `documents` e índice IVFFlat.

## 2) Rodar a API

Exportar a chave da OpenAI:
```bash
export OPENAI_API_KEY="sk-..."
```

Build & run:
```bash
mvn spring-boot:run
```

A API sobe em `http://localhost:8080`.

## 3) Testes rápidos

### Ingestão
```bash
curl -X POST http://localhost:8080/api/docs   -H "Content-Type: application/json"   -d '{"source":"manual.pdf#3", "text":"Defina API_URL e API_TOKEN no properties."}'
```

### Busca semântica
```bash
curl "http://localhost:8080/api/search?q=como%20configurar%20o%20conector&k=3"
```

### Perguntar com RAG
```bash
curl -X POST http://localhost:8080/api/ask   -H "Content-Type: application/json"   -d '{"question":"Quais variáveis preciso para configurar?", "k":4}'
```

## 4) Notas
- Ajuste `WITH (lists=...)` e rode `ANALYZE` após grandes ingestões.
- `application.yml` lê `OPENAI_API_KEY` do ambiente.
- Modelo de embeddings padrão: `text-embedding-3-small` (1536 dims).
- Modelo de chat padrão: `chatgpt-4o-mini` (mude em `application.yml` via `app.openaiChatModel`).

## 5) Estrutura
```
db/init.sql
docker-compose.yml
src/main/java/dev/demo/rag/...  # código
src/main/resources/application.yml
pom.xml
```
