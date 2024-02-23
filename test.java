openapi: 3.0.0
info:
  title: GAPI WB Rest Service
  description: API documentation for the GAPI WB Rest Service
  version: 1.0.0
paths:
  /v2/gapi-wb-rest/api/decrypt:
    post:
      summary: Decrypt a message using GAPI WB Rest
      description: Endpoint to decrypt a message using GAPI WB Rest.
      parameters:
        - name: source_id
          in: query
          description: Source ID for decryption
          required: true
          schema:
            type: string
        - name: message_len
          in: query
          description: Length of the message
          required: true
          schema:
            type: integer
        - name: enc_message
          in: query
          description: Encrypted message to be decrypted
          required: true
          schema:
            type: string
      responses:
        '200':
          description: Successful decryption
          content:
            application/json:
              example:
                status: "Success"
                dec_message: "Decrypted message"
                message_len: 20
        '400':
          description: Bad Request
          content:
            application/json:
              example:
                status: "Error"
                error_message: "Invalid request format"

  /v2/gapi-wb-rest/api/encrypt:
    post:
      summary: Encrypt a message using GAPI WB Rest
      description: Endpoint to encrypt a message using GAPI WB Rest.
      parameters:
        - name: source_id
          in: query
          description: Source ID for encryption
          required: true
          schema:
            type: string
        - name: message_len
          in: query
          description: Length of the message
          required: true
          schema:
            type: integer
        - name: plain_message
          in: query
          description: Plain message to be encrypted
          required: true
          schema:
            type: string
      responses:
        '200':
          description: Successful encryption
          content:
            application/json:
              example:
                status: "Success"
                enc_message: "Encrypted message"
                message_len: 30
        '400':
          description: Bad Request
          content:
            application/json:
              example:
                status: "Error"
                error_message: "Invalid request format"
