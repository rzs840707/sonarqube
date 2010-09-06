#
# Sonar, entreprise quality control tool.
# Copyright (C) 2009 SonarSource SA
# mailto:contact AT sonarsource DOT com
#
# Sonar is free software; you can redistribute it and/or
# modify it under the terms of the GNU Lesser General Public
# License as published by the Free Software Foundation; either
# version 3 of the License, or (at your option) any later version.
#
# Sonar is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
# Lesser General Public License for more details.
#
# You should have received a copy of the GNU Lesser General Public
# License along with Sonar; if not, write to the Free Software
# Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
#
require 'fastercsv'
require "json"

class Api::RulesController < Api::RestController

  def rest_call
    set_backward_compatibility_params
    language = params[:language] || ''
    options= {}
    if params[:plugins]
      options[:plugins] = params[:plugins].split(',') 
    else
      options[:plugins] = java_facade.getPluginsByRuleLanguage(language).collect { |plugin| plugin.getKey() }
    end
    options[:categories]=params[:categories].split(',') if params[:categories]
    options[:priorities]=params[:priorities].split(',') if params[:priorities]
    options[:status]=params[:status]
    options[:searchtext]=params[:searchtext]
    options[:include_parameters]=true
    

    if params[:profile]
      profile = RulesProfile.find_by_name_and_language(params[:profile], language)
      if profile.nil?
        rest_render([])
      else
        options[:profile]=profile
        rules = Rule.search(options)
        rest_render(rules, profile)
      end
    else
      rules = Rule.search(options)
      rest_render(rules)
    end
  end
  
  def set_backward_compatibility_params
    params[:categories]=params[:category] if params[:category]
    params[:plugins]=params[:plugin] if params[:plugin]
    params[:priorities]=params[:levels] if params[:levels]
  end


  private

  def rest_render(rules=[], profile=nil)
    respond_to do |format|
      format.json{ render :json => rest_to_json(rules, profile) }
      format.xml { render :xml => rest_to_xml(rules, profile) }
      format.csv {
        send_data(rest_to_csv(rules, profile),
          :type => 'text/csv; charset=utf-8; header=present',
          :disposition => 'attachment; filename=rules.csv')
      }
    end
  end

  def rest_to_json(rules, profile)
    JSON(rules.collect{|rule| rule.to_hash_json(profile)})
  end

  def rest_to_xml(rules, profile)
    xml = Builder::XmlMarkup.new(:indent => 0)
    xml.instruct!
    xml.rules do
      rules.each do |rule|
        xml << rule.to_xml(profile)
      end
    end
  end

  def rest_to_csv(rules, profile)
    FasterCSV.generate do |csv|
      header = ["title", "key", "category", "plugin"]
      header.concat(["priority","status"]) if profile
      csv << header
      rules.each do |rule|
        csv << rule.to_csv(profile)
      end
    end
  end

end