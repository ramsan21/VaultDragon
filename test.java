Sub ExtractSourceAddress()
    Dim ws As Worksheet
    Dim lastRow As Long
    Dim i As Long
    Dim logEntry As String
    Dim startPos As Integer
    Dim endPos As Integer
    Dim sourceAddress As String
    
    ' Set the worksheet where data is present
    Set ws = ThisWorkbook.Sheets("Sheet1") ' Change "Sheet1" to your actual sheet name

    ' Find the last row in column A
    lastRow = ws.Cells(ws.Rows.Count, 1).End(xlUp).Row

    ' Loop through each row in column A
    For i = 2 To lastRow ' Assuming row 1 has headers, start from row 2
        logEntry = ws.Cells(i, 1).Value
        startPos = InStr(logEntry, "source-address=""")
        
        ' If "source-address=" is found
        If startPos > 0 Then
            startPos = startPos + 15 ' Move to start of IP
            endPos = InStr(startPos, logEntry, """") ' Find closing quote
            If endPos > 0 Then
                sourceAddress = Mid(logEntry, startPos, endPos - startPos)
                ws.Cells(i, 2).Value = sourceAddress ' Place the value in Column B
            End If
        End If
    Next i

    MsgBox "Source Address Extraction Completed!", vbInformation
End Sub