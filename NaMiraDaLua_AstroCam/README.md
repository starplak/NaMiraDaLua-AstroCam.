# Na Mira da Lua AstroCam

Projeto inicial Android para o aplicativo **Na Mira da Lua AstroCam**.

## Estado desta versão

- Estrutura Android/Kotlin pronta para abrir no Android Studio.
- Tela inicial em português.
- Detecção/listagem de dispositivos USB conectados via `UsbManager`.
- Permissão USB solicitada ao tocar em um dispositivo.
- GitHub Actions configurado para gerar um APK Debug.
- **Ainda não inclui visualização UVC ao vivo nem controles de imagem da SVBONY SV105.** Esta é a base de compilação; a integração da câmera será a próxima etapa, depois de validar o modelo USB e a compatibilidade UVC.

## Requisitos

- Android Studio recente
- JDK 17
- Android SDK Platform 35
- Gradle 8.9 (o workflow do GitHub configura essa versão)

## Abrir e compilar localmente

1. Extraia todo o ZIP.
2. No GitHub, envie o conteúdo desta pasta para a raiz do repositório (não envie o ZIP fechado).
3. No Android Studio, abra a pasta extraída.
4. Aguarde a sincronização do Gradle.
5. Execute `app` em um celular Android ou use **Build > Build Bundle(s) / APK(s) > Build APK(s)**.

## Compilar pelo GitHub

1. Envie todos os arquivos e pastas para a raiz do repositório.
2. Abra a aba **Actions**.
3. Se solicitado, habilite GitHub Actions.
4. Execute o workflow **Build AstroCam APK** (ou faça um push para `main`).
5. Ao terminar, abra a execução concluída e baixe o artefato `AstroCam-debug-apk`.

## Próxima etapa

Implementar o preview UVC e os controles de captura/processamento, testando com a SVBONY SV105 no Realme C13 via USB OTG.
