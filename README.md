# Steganography 🔒🖼️

The following project is part of the Cryptography and Security course.

## Objective

- Gain an insight into the field of steganography and its applications.
- Experiment with different techniques of information hiding using `.bmp` files.
- Implement and analyze an algorithm published in a scientific paper.

## Requirements and code generation
You must have installed:
- Java 19
- Maven

You must then run the following command: 
```shell
maven package
```

## Embedding
Embedding is the process of hiding files information, in this case files, within other files. 
In order to embed in a `.bmp` file, you must run the following command:
```shell
java -classpath ./target/classes ar.edu.itba.Main -embed -in <file> -p <in.bmp> -out <out.bmp> -steg <LSB1|LSB4|LSBI> [-a <aes128|aes192|aes256|des>] [-m <cbc|cfb|ofb|ecb>] [-pass <password>]
```

- `in`: the file that you want to hide in the `.bmp` image.
- `p`: the `.bmp` image that will be used during the steganography (it will not be modified).
- `out`: the path of the new image, which contains the embedded file.
- `steg`: the steganography method to be used. 
- `-a` (optional): the encryption scheme to be used. 
- `-m` (optional): the mode for the encryption scheme.
- `-pass` (optional): the password to be used for the encryption.


## Extracting
Extracting, is the reverse of embedding. Given a file, in this case an image, we extract a file hidden within it. In order to extract in a `.bmp` file, you must run the following command:
```shell
java -classpath ./target/classes ar.edu.itba.Main -extract -p <in.bmp> -out <file_name> -steg <LSB1|LSB4|LSBI> [-a <aes128|aes192|aes256|des>] [-m <cbc|cfb|ofb|ecb>] [-pass <password>]
```

- `p`: the `.bmp` image that will be extracted from (it will not be modified).
- `out`: the path of file that will be extracted from the image (THE EXTENSION IS NOT NEEDED).
- `steg`: the steganography method to be used.
- `-a` (optional): the encryption scheme to be used.
- `-m` (optional): the mode for the encryption scheme.
- `-pass` (optional): the password to be used for the encryption.

