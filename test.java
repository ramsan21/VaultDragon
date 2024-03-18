openapi: 3.0.0
info:
  title: PGP API
  version: 1.0.0
  description: API for PGP encryption and decryption
paths:
  /v2/pgp/api/encryptFile:
    post:
      summary: Encrypt a file
      requestBody:
        required: true
        content:
          multipart/form-data:
            schema:
              type: object
              properties:
                inputFile:
                  type: string
                  format: binary
                outputFile:
                  type: string
                  format: binary
                identity:
                  type: string
                encAlgo:
                  type: string
                armor:
                  type: boolean
                withIntegrityCheck:
                  type: boolean
      responses:
        '200':
          description: File encrypted successfully
          content:
            application/json:
              schema:
                type: object
                properties:
                  statusCode:
                    type: integer
                  statusMessage:
                    type: string
                  outputFile:
                    type: string
  /v2/pgp/api/decryptFile:
    post:
      summary: Decrypt a file
      requestBody:
        required: true
        content:
          multipart/form-data:
            schema:
              type: object
              properties:
                inputFile:
                  type: string
                  format: binary
                outputFile:
                  type: string
                  format: binary
                groupId:
                  type: string
                format:
                  type: string
      responses:
        '200':
          description: File decrypted successfully
          content:
            application/json:
              schema:
                type: object
                properties:
                  statusCode:
                    type: integer
                  statusMessage:
                    type: string
                  outputFile:
                    type: string
  /v2/pgp/api/signFile:
    post:
      summary: Sign a file
      requestBody:
        required: true
        content:
          multipart/form-data:
            schema:
              type: object
              properties:
                inputFile:
                  type: string
                  format: binary
                outputFile:
                  type: string
                  format: binary
                identity:
                  type: string
                armor:
                  type: boolean
                encAlgo:
                  type: string
                typeDesc:
                  type: string
      responses:
        '200':
          description: File signed successfully
          content:
            application/json:
              schema:
                type: object
                properties:
                  statusCode:
                    type: integer
                  statusMessage:
                    type: string
                  outputFile:
                    type: string
  /v2/pgp/api/verifyFile:
    post:
      summary: Verify a file
      requestBody:
        required: true
        content:
          multipart/form-data:
            schema:
              type: object
              properties:
                inputFile:
                  type: string
                  format: binary
                outputFile:
                  type: string
                  format: binary
                groupId:
                  type: string
                keyId:
                  type: string
                format:
                  type: string
      responses:
        '200':
          description: File verified successfully
          content:
            application/json:
              schema:
                type: object
                properties:
                  statusCode:
                    type: integer
                  statusMessage:
                    type: string
                  outputFile:
                    type: string
  /v2/pgp/api/signEncryptFile:
    post:
      summary: Sign and encrypt a file
      requestBody:
        required: true
        content:
          multipart/form-data:
            schema:
              type: object
              properties:
                inputFile:
                  type: string
                  format: binary
                outputFile:
                  type: string
                  format: binary
                bankIdentity:
                  type: string
                clientIdentity:
                  type: string
                armor:
                  type: boolean
                encAlgo:
                  type: string
      responses:
        '200':
          description: File signed and encrypted successfully
          content:
            application/json:
              schema:
                type: object
                properties:
                  statusCode:
                    type: integer
                  statusMessage:
                    type: string
                  outputFile:
                    type: string
  /v2/pgp/api/DecryptVerifyRequest:
    post:
      summary: Decrypt and verify a request
      requestBody:
        required: true
        content:
          multipart/form-data:
            schema:
              type: object
              properties:
                inputFile:
                  type: string
                  format: binary
                outputFile:
                  type: string
                  format: binary
                groupId:
                  type: string
                keyId:
                  type: string
                format:
                  type: string
      responses:
        '200':
          description: Request decrypted and verified successfully
          content:
            application/json:
              schema:
                type: object
                properties:
                  statusCode:
                    type: integer
                  statusMessage:
                    type: string
                  outputFile:
                    type: string
  /v2/pgp/api/importBankKey:
    post:
      summary: Import a bank key
      requestBody:
        required: true
        content:
          multipart/form-data:
            schema:
              type: object
              properties:
                filePath:
                  type: string
                fileBase64:
                  type: string
                fileByte:
                  type: string
      responses:
        '200':
          description: Bank key imported successfully
          content:
            application/json:
              schema:
                type: object
                properties:
                  statusCode:
                    type: integer
                  statusMessage:
                    type: string
  /v2/pgp/api/bankKeyByUser/{user}:
    get:
      summary: Get bank key by user
      parameters:
        - name: user
          in: path
          required: true
          schema:
            type: string
      responses:
        '200':
          description: Bank key retrieved successfully
          content:
            application/json:
              schema:
                type: object
                properties:
                  userBankkey:
                    type: object
                    properties:
                      user:
                        type: string
                      groupId:
                        type: string
                      keyId:
                        type: string
                      publickeyData:
                        type: string
                      privatekey:
                        type: string
                      expiryDate:
                        type: string
                      createdon:
                        type: string
  /v2/pgp/api/bankKeyByKeyId/{keyId}:
    get:
      summary: Get bank key by key ID
      parameters:
        - name: keyId
          in: path
          required: true
          schema:
            type: string
      responses:
        '200':
          description: Bank key retrieved successfully
          content:
            application/json:
              schema:
                type: object
                properties:
                  bankKeys:
                    type: array
                    items:
                      $ref: '#/components/schemas/BankKey'
  /v2/pgp/api/deleteBankKey/{user}:
    delete:
      summary: Delete bank key by user
      parameters:
        - name: user
          in: path
          required: true
          schema:
            type: string
      responses:
        '200':
          description: Bank key deleted successfully
          content:
            application/json:
              schema:
                type: object
                properties:
                  statusCode:
                    type: integer
                  statusMessage:
                    type: string
  /v2/pgp/api/updateBankPrivateKey/{user}/{privateKey}:
    put:
      summary: Update bank private key by user
      parameters:
        - name: user
          in: path
          required: true
          schema:
            type: string
        - name: privateKey
          in: path
          required: true
          schema:
            type: string
      responses:
        '200':
          description: Bank private key updated successfully
          content:
            application/json:
              schema:
                type: object
                properties:
                  statusCode:
                    type: integer
                  statusMessage:
                    type: string
  /v2/pgp/api/importCustomerkey:
    post:
      summary: Import a customer key
      requestBody:
        required: true
        content:
          multipart/form-data:
            schema:
              type: object
              properties:
                filePath:
                  type: string
                fileBase64:
                  type: string
                fileByte:
                  type: string
      responses:
        '200':
          description: Customer key imported successfully
          content:
            application/json:
              schema:
                type: object
                properties:
                  statusCode:
                    type: integer
                  statusMessage:
                    type: string
  /v2/pgp/api/customerKeyByUser/{user}:
    get:
      summary: Get customer key by user
      parameters:
        - name: user
          in: path
          required: true
          schema:
            type: string
      responses:
        '200':
          description: Customer key retrieved successfully
          content:
            application/json:
              schema:
                type: object
                properties:
                  statusCode:
                    type: integer
                  statusMessage:
                    type: string
  /v2/pgp/api/customerKeyByKeyId/{keyId}:
    get:
      summary: Get customer key by key ID
      parameters:
        - name: keyId
          in: path
          required: true
          schema:
            type: string
      responses:
        '200':
          description: Customer key retrieved successfully
          content:
            application/json:
              schema:
                type: object
                properties:
                  statusCode:
                    type: integer
                  statusMessage:
                    type: string
  /v2/pgp/api/deleteCustKey/{user}:
    delete:
      summary: Delete customer key by user
      parameters:
        - name: user
          in: path
          required: true
          schema:
            type: string
      responses:
        '200':
          description: Customer key deleted successfully
          content:
            application/json:
              schema:
                type: object
                properties:
                  statusCode:
                    type: integer
                  statusMessage:
                    type: string
  /v2/pgp/api/keyData:
    post:
      summary: Get key data
      requestBody:
        required: true
        content:
          multipart/form-data:
            schema:
              type: object
              properties:
                filePath:
                  type: string
                fileBase64:
                  type: string
                fileByte:
                  type: string
      responses:
        '200':
          description: Key data retrieved successfully
          content:
            application/json:
              schema:
                type: array
                items:
                  $ref: '#/components/schemas/KeyResponse'
  /v2/pgp/api/fileData:
    post:
      summary: Get file data
      requestBody:
        required: true
        content:
          application/json:
            schema:
              type: object
              properties:
                filePath:
                  type: string
      responses:
        '200':
          description: File data retrieved successfully
          content:
            application/octet-stream:
              schema:
                type: string
                format: binary
  /v2/pgp/api/keyDataByUser/{user}:
    get:
      summary: Get key data by user
      parameters:
        - name: user
          in: path
          required: true
          schema:
            type: string
      responses:
        '200':
          description: Key data retrieved successfully
          content:
            application/json:
              schema:
                type: array
                items:
                  $ref: '#/components/schemas/KeyResponse'
components:
  schemas:
    BankKey:
      type: object
      properties:
        user:
          type: string
        groupId:
          type: string
        keyId:
          type: string
        publickeyData:
          type: string
        privatekey:
          type: string
        expiryDate:
          type: string
        createdon:
          type: string
    KeyResponse:
      type: object
      properties:
        userIds:
          type: string
        keyId:
          type: string
        algorithm:
          type: string
        creationTime:
          type: string
        expiryDate:
          type: string
        validSeconds:
          type: integer
        isEncryptionKey:
          type: boolean
        isMasterKey:
          type: boolean
