- name: Remove run.sh and rename catalyst_run.sh to run.sh if ENV is htg
  when: ENV == 'htg'
  block:
    - name: Remove existing run.sh
      file:
        path: "{{ temp_dir }}/run.sh"
        state: absent

    - name: Rename catalyst_run.sh to run.sh
      command: mv catalyst_run.sh run.sh
      args:
        chdir: "{{ temp_dir }}"