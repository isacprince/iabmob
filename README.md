# IAB Mob

## Visão Geral

**IAB Mob** é um aplicativo Android que oferece uma solução integrada para calcular rotas e estimar o tempo de viagem entre dois locais. O aplicativo utiliza o Google Maps para apresentar um mapa interativo, permitindo aos usuários inserir uma localização de partida e um destino para obter a melhor rota disponível. Ele fornece informações sobre o tempo estimado de viagem e a melhor opção de transporte.

## Funcionalidades

- **Autenticação:** Sistema de login e registro para usuários.
- **Mapa Interativo:** Exibição de mapa usando Google Maps com a capacidade de traçar rotas entre duas localidades.
- **Cálculo de Rotas:** Calcula rotas entre a localização inicial e o destino usando a API do Google Directions.
- **Estimativas de Tempo:** Mostra o tempo estimado de viagem e sugere o melhor meio de transporte.
- **Interface de Usuário Amigável:** Interface clara e acessível com navegação fácil entre telas.

## Tecnologias Utilizadas

- **Linguagem:** Kotlin
- **Arquitetura:** MVVM
- **Bibliotecas:**
  - Android Jetpack (Navigation, ViewModel, LiveData)
  - Google Maps SDK para Android
  - OkHttp para solicitações de rede
  - Firebase Authentication (opcional)
- **Plataforma:** Android SDK

## Pré-requisitos

- Android Studio Arctic Fox ou superior
- Android SDK compilado para a versão 34 ou superior
- Chave de API do Google Maps

## Instalação

1. **Clone o repositório:**

   \`\`\`bash
   git clone https://github.com/isacprince/iabmob.git
   cd iabmob
   \`\`\`

2. **Configuração do Projeto:**

   - Abra o Android Studio e importe o projeto.
   - No arquivo \`local.properties\`, adicione sua chave de API do Google Maps:

     \`\`\`properties
     MAPS_API_KEY=**********************************************
     \`\`\`

3. **Sincronize o projeto** com os arquivos \`build.gradle\`.

4. **Execute o aplicativo** em um dispositivo ou emulador Android.

## Como Usar

1. **Autenticação:**
   - Faça login com suas credenciais existentes ou registre uma nova conta.

2. **Mapa e Cálculo de Rotas:**
   - Insira a localização de partida e destino nos campos apropriados.
   - Clique no botão "Calcular Rota" para ver a rota e estimativas de tempo no mapa.

3. **Resultados:**
   - Acesse a aba "Resultados" para ver detalhes sobre a viagem estimada e a melhor opção de transporte.

## Contribuição

Contribuições são bem-vindas! Se você tiver sugestões ou encontrar algum problema, sinta-se à vontade para abrir uma [issue](https://github.com/isacprince/iabmob/issues) ou enviar um pull request.

### Passos para Contribuir

1. Faça um fork do projeto.
2. Crie uma nova branch (\`git checkout -b feature/nova-funcionalidade\`).
3. Commit suas mudanças (\`git commit -m 'Adiciona nova funcionalidade'\`).
4. Push para a branch (\`git push origin feature/nova-funcionalidade\`).
5. Abra um Pull Request.

## Contato

Para mais informações, entre em contato pelo Teams.
"""