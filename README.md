# Midia Indoor Player — App Android (WebView + Auto-inicio)

App simples que abre o player (o `player_tv.html` que ja esta no seu servidor)
em tela cheia, sem barra do Android, e liga sozinho quando a TV Box reinicia.

## O que ele faz

- Abre a URL do seu player em tela cheia (sem status bar, sem barra de navegacao)
- Se a internet cair no meio, tenta recarregar sozinho a cada 8 segundos
- Quando a TV Box liga (ou reinicia), abre o app automaticamente — sem precisar
  de um segundo app complementar de "AutoStart"
- Funciona em Android 4.4 em diante (cobre praticamente qualquer TV Box do mercado)
- Na primeira vez que abrir, pede pra voce colar o link do player. Depois disso
  ele guarda e abre direto nesse link sempre. Pra trocar o link depois, e so dar
  um toque longo em qualquer parte da tela.

## Passo a passo pra gerar o `.apk` (sem instalar nada no computador)

1. Crie uma conta gratuita em **https://github.com** (se ainda nao tiver)
2. Crie um repositorio novo, **privado** ou publico, tanto faz (botao verde
   "New" no canto superior direito do GitHub)
3. Dentro do repositorio, use o botao **"Add file" -> "Upload files"** e arraste
   **esta pasta inteira** (`midia-indoor-player-app`) pra dentro — incluindo a
   pasta `.github` (ela pode ficar escondida no explorador de arquivos, cuidado
   pra nao esquecer ela)
4. Clique em **"Commit changes"** pra confirmar o envio
5. Va na aba **"Actions"** do repositorio — o robozinho de build ("Gerar APK")
   comeca sozinho. Leva uns 3 a 6 minutos.
6. Quando o circulo verde aparecer (concluido), clique em cima do build que
   rodou, desça ate **"Artifacts"** e baixe o **MidiaIndoorPlayer-apk** — vem
   como um `.zip`, dentro dele esta o `app-debug.apk`

## Passo a passo pra instalar na TV Box

1. Copie o `app-debug.apk` pra um pendrive, ou suba num link e baixe direto
   na TV Box pelo navegador dela
2. Na TV Box, em **Configuracoes -> Seguranca**, ative **"Fontes desconhecidas"**
   (ou "Instalar apps desconhecidos") — sem isso o Android bloqueia a instalacao
3. Abra o `.apk` e instale
4. Na primeira abertura, cole o link do seu `player_tv.html` (o mesmo que voce
   testou no Fully Kiosk) e toque em "Salvar e abrir"
5. Reinicie a TV Box uma vez pra confirmar que ele volta sozinho no player

## Se algum aparelho nao aceitar o auto-inicio normal

Alguns fabricantes de TV Box bloqueiam esse gatilho de qualquer app de
terceiros (e o motivo de existir a versao "Launcher" no concorrente que voce
mandou). Se isso acontecer num aparelho especifico, e so avisar que eu ajusto
o `AndroidManifest.xml` pra esse app virar o Launcher (tela inicial) padrao
daquele aparelho — ai o auto-inicio fica garantido pelo proprio sistema, sem
excecao.
