- name: Copy the files with secrets to server
  copy:
    src: "{{ item }}"
    dest: >-
      {{ temp_dir }}/{{ item
        | regex_replace('^' + STAGING_DIR + '/', '')
        | regex_replace('/' + ENV + '/', '/' if ENV in ['STG', 'SIT3'] else '/' + ENV + '/')
      }}
    owner: starsswb
    group: starsed
    mode: 0755
  with_fileglob:
    - "{{ STAGING_DIR }}/{{ PKG_NAME }}/{{ PKG_VERSION }}/keys/{{ ENV }}/*.jks"
    - "{{ STAGING_DIR }}/{{ PKG_NAME }}/{{ PKG_VERSION }}/conf/{{ ENV }}/*.properties"
    - "{{ STAGING_DIR }}/{{ PKG_NAME }}/{{ PKG_VERSION }}/conf/{{ ENV }}/*.yaml"
    - "{{ STAGING_DIR }}/{{ PKG_NAME }}/{{ PKG_VERSION }}/*.ksh"
    - "{{ STAGING_DIR }}/{{ PKG_NAME }}/{{ PKG_VERSION }}/*.sh"