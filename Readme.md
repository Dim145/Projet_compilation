## Projet final de Compilation
Utilisation de processing pour afficher les résultats.

Les fichiers doivent se trouver dans le dossier data à la racine de lancement du projet.

Si le text est trop long, il est possible de faire défilé l'écran avec la
molette de la souris.

ex:si le projet lancé dans rep `/user/test/projetCompil`  
les fichiers devront être dans `/user/test/projetCompil/data`

commande run: `maven compile exec:java -Dfile.encoding=utf-8 -Dexec.mainClass=Main`
commande jar: `maven clean compile assembly:single`