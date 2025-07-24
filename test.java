- name: Remove run.sh and rename ctrun.sh to run.sh if ENV is STG or SIT3
  when: ENV in ['STG', 'SIT3']
  block:

    - name: Remove existing run.sh
      file:
        path: "{{ deploy_dir }}/current/run.sh"
        state: absent

    - name: Check if ctrun.sh exists in temp_dir
      stat:
        path: "{{ temp_dir }}/ctrun.sh"
      register: ctrun_file

    - name: Rename ctrun.sh to run.sh if it exists
      command: mv ctrun.sh "{{ deploy_dir }}/current/run.sh"
      args:
        chdir: "{{ temp_dir }}"
      when: ctrun_file.stat.exists