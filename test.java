openapi: 3.0.0
info:
  title: PGP REST Global Service
  description: API documentation for the PGP REST Global Service
  version: 1.0.0
paths:
  /v2/pgp/api/encryptFile:
    post:
      summary: Encrypt a file using PGP
      description: Endpoint to encrypt a file using PGP encryption.
      parameters:
        - name: inputFile
          in: query
          description: Path to the input file to be encrypted
          required: true
          schema:
            type: string
        - name: outputFile
          in: query
          description: Path to the output encrypted file
          required: true
          schema:
            type: string
        - name: identity
          in: query
          description: PGP identity (user ID) for encryption
          required: true
          schema:
            type: string
        - name: encAlgo
          in: query
          description: Encryption algorithm (e.g., RSA, ElGamal)
          required: true
          schema:
            type: string
        - name: armor
          in: query
          description: Include ASCII armor in the output (true/false)
          required: true
          schema:
            type: boolean
        - name: withIntegrityCheck
          in: query
          description: Add integrity check information (true/false)
          required: true
          schema:
            type: boolean
      responses:
        '200':
          description: Successful encryption
          content:
            application/json:
              example:
                statusCode: 200
                successMessage: "File encrypted successfully"
                outFileName: "encrypted_output.asc"
        '400':
          description: Bad Request
          content:
            application/json:
              example:
                statusCode: 400
                errorMessage: "Invalid request format"

  /v2/pgp/api/decryptFile:
    post:
      summary: Decrypt a file using PGP
      description: Endpoint to decrypt a file using PGP decryption.
      parameters:
        - name: inputFile
          in: query
          description: Path to the input file to be decrypted
          required: true
          schema:
            type: string
        - name: outputFile
          in: query
          description: Path to the output decrypted file
          required: true
          schema:
            type: string
        - name: groupId
          in: query
          description: Group ID for decryption
          required: true
          schema:
            type: string
        - name: format
          in: query
          description: Format for decryption
          required: true
          schema:
            type: string
      responses:
        '200':
          description: Successful decryption
          content:
            application/json:
              example:
                statusCode: 200
                successMessage: "File decrypted successfully"
                outFileName: "decrypted_output.txt"
        '400':
          description: Bad Request
          content:
            application/json:
              example:
                statusCode: 400
                errorMessage: "Invalid request format"

  /v2/pgp/api/signRequest:
    post:
      summary: Sign a request using PGP
      description: Endpoint to sign a request using PGP digital signature.
      parameters:
        - name: inputFile
          in: query
          description: Path to the input file to be signed
          required: true
          schema:
            type: string
        - name: outputFile
          in: query
          description: Path to the output signed file
          required: true
          schema:
            type: string
        - name: identity
          in: query
          description: PGP identity (user ID) for signing
          required: true
          schema:
            type: string
        - name: armor
          in: query
          description: Include ASCII armor in the output (true/false)
          required: true
          schema:
            type: boolean
        - name: hasAlgo
          in: query
          description: Whether the signature has an algorithm
          required: true
          schema:
            type: boolean
        - name: typeDesc
          in: query
          description: Description of the signature type
          required: true
          schema:
            type: string
      responses:
        '200':
          description: Successful signing
          content:
            application/json:
              example:
                statusCode: 200
                successMessage: "Request signed successfully"
                outFileName: "signed_request.pgp"
        '400':
          description: Bad Request
          content:
            application/json:
              example:
                statusCode: 400
                errorMessage: "Invalid request format"

  /v2/pgp/api/verifyRequest:
    post:
      summary: Verify a request using PGP
      description: Endpoint to verify a request using PGP digital signature.
      parameters:
        - name: inputFile
          in: query
          description: Path to the input file to be verified
          required: true
          schema:
            type: string
        - name: outputFile
          in: query
          description: Path to the output verification result file
          required: true
          schema:
            type: string
        - name: groupId
          in: query
          description: Group ID for verification
          required: true
          schema:
            type: string
        - name: keyId
          in: query
          description: Key ID for verification
          required: true
          schema:
            type: string
        - name: format
          in: query
          description: Format for verification
          required: true
          schema:
            type: string
      responses:
        '200':
          description: Successful verification
          content:
            application/json:
              example:
                statusCode: 200
                successMessage: "Request verified successfully"
                outFileName: "verification_result.txt"
        '400':
          description: Bad Request
          content:
            application/json:
              example:
                statusCode: 400
                errorMessage: "Invalid request format"

  /v2/pgp/api/signEncryptRequest:
    post:
      summary: Sign and encrypt a request using PGP
      description: Endpoint to sign and encrypt a request using PGP.
      parameters:
        - name: inputFile
          in: query
          description: Path to the input file to be signed and encrypted
          required: true
          schema:
            type: string
        - name: outputFile
          in: query
          description: Path to the output signed and encrypted file
          required: true
          schema:
            type: string
        - name: bIdentity
          in: query
          description: PGP identity for the first participant
          required: true
          schema:
            type: string
        - name: cIdentity
          in: query
          description: PGP identity for the second participant
          required: true
          schema:
            type: string
        - name: armor
          in: query
          description: Include ASCII armor in the output (true/false)
          required: true
          schema:
            type: boolean
        - name: encyAlgo
          in: query
          description: Encryption algorithm (e.g., RSA, ElGamal)
          required: true
          schema:
            type: string
        - name: hasAlgo
          in: query
          description: Whether the signature has an algorithm
          required: true
          schema:
            type: boolean
      responses:
        '200':
          description: Successful signing and encryption
          content:
            application/json:
              example:
                statusCode: 200
                successMessage: "Request signed and encrypted successfully"
                outFileName: "signed_encrypted_request.pgp"
        '400':
          description: Bad Request
          content:
            application/json:
              example:
                statusCode: 400
                errorMessage: "Invalid request format"

  /v2/pgp/api/decryptVerifyRequest:
    post:
      summary: Decrypt and verify a request using PGP
      description: Endpoint to decrypt and verify a request using PGP.
      parameters:
        - name: inputFile
          in: query
          description: Path to the input file to be decrypted and verified
          required: true
          schema:
            type: string
        - name: outputFile
          in: query
          description: Path to the output decrypted and verification result file
          required: true
          schema:
            type: string
        - name: bIdentity
          in: query
          description: PGP identity for the first participant
          required: true
          schema:
            type: string
        - name: cIdentity
          in: query
          description: PGP identity for the second participant
          required: true
          schema:
            type: string
        - name: armor
          in: query
          description: Include ASCII armor in the output (true/false)
          required: true
          schema:
            type: boolean
        - name: encyAlgo
          in: query
          description: Encryption algorithm (e.g., RSA, ElGamal)
          required: true
          schema:
            type: string
        - name: hasAlgo
          in: query
          description: Whether the signature has an algorithm
          required: true
          schema:
            type: boolean
      responses:
        '200':
          description: Successful decryption and verification
          content:
            application/json:
              example:
                statusCode: 200
                successMessage: "Request decrypted and verified successfully"
                outFileName: "decrypted_verification_result.txt"
        '400':
          description: Bad Request
          content:
            application/json:
              example:
                statusCode: 400
                errorMessage: "Invalid request format"
