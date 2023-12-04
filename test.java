@Test
    public void testPatchGroupWithSuccess() {
        String groupId = "testGroupId";
        ApiBankingDetail detail = ApiBankingDetail.builder()
                .apiBankingEnabled("Y")
                .certificateFingerprint("abcd123")
                .publicKey("guLKd™701WosszKjgpz3Ig==")
                .webhookUr1("testurl")
                .build();

        ResponseEntity<CADMGroupDetail> groupResponseEntity = new ResponseEntity<>(CADMGroupDetail.builder()
                .groupId("COPEXTGP")
                .groupName("COPExternalUsers")
                .countryCode("SG | SINGAPORE")
                .s2bmarketSegment("K Global Corporate")
                .contactNo("12345")
                .ngClientFlag("N")
                .build(), HttpStatus.OK);

        ResponseEntity<Map> responseResponseEntity = new ResponseEntity<>(HttpStatus.OK);

        when(restTemplate.getForEntity(url + GROUP_API + "/" + groupId, CADMGroupDetail.class)).thenReturn(groupResponseEntity);
        when(restTemplate.postForEntity(url + GROUP_API, any(HttpEntity.class), eq(Map.class))).thenReturn(responseResponseEntity);

        boolean result = cadmClient.patchGroup(groupId, detail);

        assertTrue(result);

        ArgumentCaptor<HttpEntity<Object>> requestCaptor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).postForEntity(eq(url + GROUP_API), requestCaptor.capture(), eq(Map.class));

        CADMGroupDetail group = (CADMGroupDetail) requestCaptor.getValue().getBody();
        assertEquals(detail, group.getApiservice());

        HttpHeaders headers = requestCaptor.getValue().getHeaders();
        assertEquals(MediaType.APPLICATION_JSON, headers.getContentType());
        assertEquals(ImmutableList.of(MediaType.APPLICATION_JSON), headers.getAccept());
    }

    @Test
    public void testPatchGroupWithFailedGetGroup() {
        String groupId = "testGroupId";
        ApiBankingDetail detail = ApiBankingDetail.builder()
                .apiBankingEnabled("Y")
                .certificateFingerprint("abcd123")
                .publicKey("guLKd™701WosszKjgpz3Ig==")
                .webhookUr1("testurl")
                .build();

        ResponseEntity<CADMGroupDetail> groupResponseEntity = new ResponseEntity<>(HttpStatus.NOT_FOUND);

        when(restTemplate.getForEntity(url + GROUP_API + "/" + groupId, CADMGroupDetail.class)).thenReturn(groupResponseEntity);

        boolean result = cadmClient.patchGroup(groupId, detail);

        assertFalse(result);

        verify(restTemplate, never()).postForEntity(anyString(), any(HttpEntity.class), anyClass());
    }
